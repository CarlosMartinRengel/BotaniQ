package com.botaniq.data.remote

data class ForecastResponse(
    val list: List<ForecastItem> // Bloques cada 3 horas
)

data class ForecastItem(
    val dt: Long, // Timestamp
    val main: MainData, // Aquí están temp y humidity
    val dtTxt: String // Fecha
)
