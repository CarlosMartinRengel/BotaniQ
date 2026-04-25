package com.botaniq.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.botaniq.data.local.entities.WeatherCacheEntity

@Dao
interface WeatherCacheDao {
    @Query("SELECT * FROM weather_cache")
    suspend fun getWeatherCache(): WeatherCacheEntity?

    @Query("SELECT tempAvg FROM weather_cache")
    suspend fun getTempAvg(): Double?

    @Query("SELECT humidityAvg FROM weather_cache")
    suspend fun getHumidityAvg(): Double?

    @Query("SELECT lastUpdate FROM weather_cache")
    suspend fun getLastUpdate(): Long?
}