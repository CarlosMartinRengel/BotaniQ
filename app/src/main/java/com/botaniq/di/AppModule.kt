package com.botaniq.di

import android.content.Context
import com.botaniq.data.local.BotaniQDatabase
import com.botaniq.data.local.dao.PlantDao
import com.botaniq.data.local.dao.SpeciesInfoDao
import com.botaniq.data.repository.PlantRepository
import kotlinx.coroutines.CoroutineScope

object AppModule {
    // Obtiene la base de datos
    fun provideDatabase(context: Context, scope: CoroutineScope): BotaniQDatabase {
        return BotaniQDatabase.getDatabase(context, scope)
    }

    // 2. Proveer el DAO de Plantas
    fun providePlantDao(db: BotaniQDatabase): PlantDao = db.plantDao()
    fun provideSpeciesInfoDao(db: BotaniQDatabase): SpeciesInfoDao = db.speciesInfoDao()


    // 4. Proveer el Repositorio con TODAS sus dependencias
    fun providePlantRepository(
        plantDao: PlantDao,
        speciesInfoDao: SpeciesInfoDao
    ): PlantRepository {
        return PlantRepository(plantDao, speciesInfoDao)
    }
}