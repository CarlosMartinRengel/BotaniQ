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
import com.botaniq.data.remote.RetrofitInstance
import com.botaniq.data.remote.WeatherApiService
import com.botaniq.utils.workers.WateringWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
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
            plantDao.insertPlant(plant)
        }
    }

    suspend fun updatePlant(plant: PlantEntity) {
        withContext(Dispatchers.IO) {
            plantDao.updatePlant(plant)
        }
    }


    suspend fun confirmWatering(plant: PlantEntity, city: String) {
        withContext(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            var daysAdded: Double

            try {
                val forecastResponse = RetrofitInstance.api.getForecastWeather(city + ",ES")

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

                Log.d("ALGORITMO_RIEGO", "--- RESULTADOS DEL CÁLCULO ---")
                Log.d("ALGORITMO_RIEGO", "Días base de la planta: ${plant.baseWaterFreq}")
                Log.d("ALGORITMO_RIEGO", "Temperatura media (Forecast): $avgTemp")
                Log.d("ALGORITMO_RIEGO", "Humedad media (Forecast): $avgHumidity")
                Log.d("ALGORITMO_RIEGO", "Días calculados finalmente: $daysAdded")
                Log.d("ALGORITMO_RIEGO", "Próxima fecha exacta: $nextDateFormatted")
                Log.d("ALGORITMO_RIEGO", "------------------------------")

            } catch (e: Exception) {
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

        // En caso de que la fecha salga mal
        if (delayInMillis <= 0) return

        val inputData = Data.Builder()
            .putString("PLANT_NAME", plantName)
            .putInt("PLANT_ID", plantId)
            .build()

        // Creamos la petición de trabajo de un solo uso con el retraso calculado
        val workRequest = OneTimeWorkRequestBuilder<WateringWorker>()
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        // enqueueUniqueWork con REPLACE permite que WorkManager borre la notificacion antigua y ponga
        // una nueva si el usuario riega la planta antes de tiempo
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
        internal fun calculateDynamicDays(baseDays: Int, temp: Double, humidity: Double): Double {
            var modifier = 1.0

            // Mucho calor, se riega antes
            if (temp > 28.0) modifier -= 0.2
            if (temp < 15.0) modifier += 0.2

            // Mucha humedad, menos riego
            if (humidity > 60.0) modifier += 0.1
            if (humidity < 30.0) modifier -= 0.1

            return baseDays * modifier
        }
    }
}
