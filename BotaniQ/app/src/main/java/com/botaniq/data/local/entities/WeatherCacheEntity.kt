package com.botaniq.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey
    val id: Int = 1, // Mismo registro siempre, se sobreescribe al guardar el nuevo
    val tempAvg: Double, // Temperatura media
    val humidityAvg: Double, // Humedad media
    val lastUpdate: Long // Timestamp de la última actualización
)
