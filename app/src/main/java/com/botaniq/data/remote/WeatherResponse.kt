package com.botaniq.data.remote

data class WeatherResponse(
    val main: MainData,
    val weather: List<WeatherDescription>
)

data class MainData(
    val temp: Double,
    val humidity: Double
)

data class WeatherDescription(
    val description: String
)
