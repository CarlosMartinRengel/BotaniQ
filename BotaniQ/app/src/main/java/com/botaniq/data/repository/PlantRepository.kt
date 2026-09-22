package com.botaniq.data.repository

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.botaniq.data.local.dao.PlantDao
import com.botaniq.data.local.dao.SpeciesInfoDao
import com.botaniq.data.local.dao.WeatherCacheDao
import com.botaniq.data.local.entities.PlantEntity
import com.botaniq.data.local.entities.SpeciesInfoEntity
import com.botaniq.data.local.entities.WeatherCacheEntity
import com.botaniq.data.remote.WeatherApiService
import com.botaniq.utils.workers.WateringWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class PlantRepository(
    private val context: Context,
    private val plantDao: PlantDao,
    private val speciesInfoDao: SpeciesInfoDao,
    private val weatherCacheDao: WeatherCacheDao,
    private val weatherApi: WeatherApiService
) {

    val allPlants: Flow<List<PlantEntity>> = plantDao.getAllPlants()

    suspend fun getAllSpecies(): List<SpeciesInfoEntity> {
        return withContext(Dispatchers.IO) {
            speciesInfoDao.getAllSpecies()
        }
    }

    suspend fun registerPlant(plant: PlantEntity) {
        withContext(Dispatchers.IO) {
            // Una planta nueva no tiene fecha de próximo riego: se agenda desde ahora
            val newPlant = if (plant.nextWateringDate <= 0L) {
                plant.copy(
                    nextWateringDate =
                        initialNextWatering(plant.baseWaterFreq, System.currentTimeMillis())
                )
            } else {
                plant
            }

            // Si id == 0, Room autogenera el id; insertPlant devuelve la fila real
            val rowId = plantDao.insertPlant(newPlant)
            val plantId = if (newPlant.id > 0) newPlant.id else rowId.toInt()

            scheduleWateringNotification(plantId, newPlant.nickname, newPlant.nextWateringDate)
        }
    }

    suspend fun updatePlant(plant: PlantEntity) {
        withContext(Dispatchers.IO) {
            plantDao.updatePlant(plant)
        }
    }

    suspend fun getPlantById(id: Int): PlantEntity? {
        return withContext(Dispatchers.IO) {
            plantDao.getPlantByIdSync(id)
        }
    }


    suspend fun confirmWatering(plant: PlantEntity, city: String) {
        withContext(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            var daysAdded: Double

            try {
                val forecastResponse = weatherApi.getForecastWeather(city + ",ES")

                // Forecast recoge tramos de 3 horas, para coger 3 dias -> 72h / 3 = 24
                val upcomingForecast = forecastResponse.list.take(24)

                val avgTemp = upcomingForecast.map { it.main.temp }.average()
                val avgHumidity = upcomingForecast.map { it.main.humidity }.average()

                val cacheEntity = WeatherCacheEntity(
                    id = 1,
                    tempAvg = avgTemp,
                    humidityAvg = avgHumidity,
                    lastUpdate = currentTime
                )

                weatherCacheDao.insertWeatherCache(cacheEntity)
                daysAdded = calculateDynamicDays(plant.baseWaterFreq, avgTemp, avgHumidity)

                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val nextDateFormatted =
                    formatter.format(Date(currentTime + (daysAdded * 24 * 60 * 60 * 1000).toLong()))

                // Comprobacion en el Logcat de que los calculos son correctos
                Log.d("ALGORITMO_RIEGO", "--- RESULTADOS DEL CÁLCULO ---")
                Log.d("ALGORITMO_RIEGO", "Días base de la planta: ${plant.baseWaterFreq}")
                Log.d("ALGORITMO_RIEGO", "Temperatura media (Forecast): $avgTemp")
                Log.d("ALGORITMO_RIEGO", "Humedad media (Forecast): $avgHumidity")
                Log.d("ALGORITMO_RIEGO", "Días calculados finalmente: $daysAdded")
                Log.d("ALGORITMO_RIEGO", "Próxima fecha exacta: $nextDateFormatted")
                Log.d("ALGORITMO_RIEGO", "------------------------------")

            } catch (e: Exception) {
                Log.e("WEATHER_API", "Error al obtener clima", e)
                val cache = weatherCacheDao.getWeatherCache()
                if (cache != null) {
                    daysAdded =
                        calculateDynamicDays(plant.baseWaterFreq, cache.tempAvg, cache.humidityAvg)
                } else {
                    // Sin cache -> Frecuencia normal
                    daysAdded = plant.baseWaterFreq.toDouble()
                }
            }

            // Convertimos días a milisegundos (24h * 60m * 60s * 1000ms)
            val frequencyInMillis = (daysAdded * 24 * 60 * 60 * 1000).toLong()
            val nextWatering = currentTime + frequencyInMillis

            val updatedPlant = plant.copy(
                lastWateredDate = currentTime,
                nextWateringDate = nextWatering
            )

            plantDao.updatePlant(updatedPlant)

            // Se agenda la notificacion para el proximo riego
            scheduleWateringNotification(plant.id, plant.nickname, nextWatering)
        }
    }

    private fun scheduleWateringNotification(plantId: Int, plantName: String, nextWatering: Long) {

        val currentTime = System.currentTimeMillis()
        val delayInMillis = nextWatering - currentTime
        // Baja la notificacion a 10s
        //val delayInMillis = 10000L

        // En caso de que la fecha salga mal
        if (delayInMillis <= 0) return

        val inputData = Data.Builder()
            .putString("PLANT_NAME", plantName)
            .putInt("PLANT_ID", plantId)
            .build()

        // Petición de trabajo de un unico uso con retraso aplicado
        val workRequest = OneTimeWorkRequestBuilder<WateringWorker>()
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        // enqueueUniqueWork con ExistingWorkPolicy.REPLACE permite que WorkManager borre la notificacion antigua y ponga
        // una nueva en caso de que el usuario riegue la planta antes de tiempo
        WorkManager.getInstance(context).enqueueUniqueWork(
            "Watering_Plant_$plantId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

    }

    suspend fun getSpeciesInfo(name: String): SpeciesInfoEntity? {
        return withContext(Dispatchers.IO) {
            speciesInfoDao.getSpeciesByName(name)
        }
    }

    // Para test
    companion object {
        // Fecha de próximo riego de una planta recién registrada (aún sin regar)
        internal fun initialNextWatering(baseDays: Int, now: Long): Long =
            now + baseDays * 24L * 60 * 60 * 1000

        internal fun calculateDynamicDays(baseDays: Int, temp: Double, humidity: Double): Double {
            var modifier = 1.0

            // Al obtenerse las medias de temperaturas de  dias completos, hay que rebajar los limites de la temperatura
            // debido a las discrepancias de temperatura entre el día y la noche
            when {
                temp >= 24.0 -> modifier -= 0.3
                temp >= 20.0 -> modifier -= 0.15
                temp <= 8.0 -> modifier += 0.3
                temp <= 12.0 -> modifier += 0.2
            }

            when {
                humidity <= 35.0 -> modifier -= 0.15
                humidity <= 45.0 -> modifier -= 0.05
                humidity >= 75.0 -> modifier += 0.2
                humidity >= 65.0 -> modifier += 0.1
            }

            // Para que no pase mucho tiempo sin regarse o regandose demasiado frecuente
            return (baseDays * modifier).coerceIn(
                minimumValue = baseDays * 0.5,
                maximumValue = baseDays * 1.5
            )
        }
    }

    suspend fun deletePlantWithImages(plant: PlantEntity) {
        withContext(Dispatchers.IO) {
            plantDao.deletePlant(plant)

            plant.photoUri?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                    Log.d("CLEANUP", "Archivo eliminado al borrar planta: ${file.name}")
                }
            }
        }
    }

    suspend fun cleanImages() {
        withContext(Dispatchers.IO) {
            Log.d("CLEANUP", "Iniciando proceso de limpieza general...")
            try {
                val activePlants = plantDao.getAllPlantsSync()
                val activeUris = activePlants.mapNotNull { it.photoUri }

                val internalFolder = context.filesDir
                val filesOnDisk = internalFolder.listFiles { file ->
                    file.name.startsWith("plant_") && file.name.endsWith(".jpg")
                }

                filesOnDisk?.forEach { file ->
                    if (!activeUris.contains(file.absolutePath)) {
                        val deleted = file.delete()
                        if (deleted) Log.d("CLEANUP", "Eliminado archivo huérfano: ${file.name}")
                    }
                }

            } catch (e: Exception) {
                Log.e("CLEANUP", "Error en la limpieza de archivos", e)
            }
        }
    }
}
