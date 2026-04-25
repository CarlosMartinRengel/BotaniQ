package com.botaniq.data.repository

import com.botaniq.data.local.dao.PlantDao
import com.botaniq.data.local.dao.SpeciesInfoDao
import com.botaniq.data.local.entities.PlantEntity
import com.botaniq.data.local.entities.SpeciesInfoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PlantRepository(
    private val plantDao: PlantDao,
    private val speciesInfoDao: SpeciesInfoDao
) {

    val allPlants: Flow<List<PlantEntity>> = plantDao.getAllPlants()

    suspend fun registerPlant(plant: PlantEntity) {
        withContext(Dispatchers.IO) {
            plantDao.insertPlant(plant)
        }
    }

    suspend fun confirmWatering(plant: PlantEntity) {
        withContext(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            // Convertimos días a milisegundos (24h * 60m * 60s * 1000ms)
            val frequencyInMillis = plant.baseWaterFreq * 24L * 60L * 60L * 1000L
            val nextWatering = currentTime + frequencyInMillis

            // Copia actualizada de la planta
            val updatedPlant = plant.copy(
                lastWateredDate = currentTime,
                nextWateringDate = nextWatering
            )

            plantDao.updatePlant(updatedPlant)
        }
    }

    suspend fun getSpeciesInfo(name: String): SpeciesInfoEntity? {
        return withContext(Dispatchers.IO) {
            speciesInfoDao.getSpeciesByName(name)
        }
    }
}
