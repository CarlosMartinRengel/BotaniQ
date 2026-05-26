package com.botaniq.di

import android.content.Context
import com.botaniq.data.local.BotaniQDatabase
import com.botaniq.data.local.dao.PlantDao
import com.botaniq.data.local.dao.SpeciesInfoDao
import com.botaniq.data.local.dao.WeatherCacheDao
import com.botaniq.data.remote.RetrofitInstance
import com.botaniq.data.remote.WeatherApiService
import com.botaniq.data.repository.PlantRepository
import com.botaniq.data.tensorflow.TFLiteAnalyzer
import kotlinx.coroutines.CoroutineScope

object AppModule {
    // Obtiene la base de datos
    fun provideDatabase(context: Context, scope: CoroutineScope): BotaniQDatabase {
        return BotaniQDatabase.getDatabase(context, scope)
    }

    // Proveer el DAO de las entidades
    fun providePlantDao(db: BotaniQDatabase): PlantDao = db.plantDao()
    fun provideSpeciesInfoDao(db: BotaniQDatabase): SpeciesInfoDao = db.speciesInfoDao()
    fun provideWeatherCacheDao(db: BotaniQDatabase): WeatherCacheDao = db.weatherCacheDao()
    fun provideWeatherApi(): WeatherApiService = RetrofitInstance.api

    // Proveer el repositorio
    fun providePlantRepository(
        context: Context,
        plantDao: PlantDao,
        speciesInfoDao: SpeciesInfoDao,
        weatherCacheDao: WeatherCacheDao,
        weatherApi: WeatherApiService
    ): PlantRepository {
        return PlantRepository(context, plantDao, speciesInfoDao, weatherCacheDao, weatherApi)
    }

    // Proveer el analizador de TFLite
    fun provideTFLiteAnalyzer(context: Context): TFLiteAnalyzer {
        return TFLiteAnalyzer(context)
    }
}