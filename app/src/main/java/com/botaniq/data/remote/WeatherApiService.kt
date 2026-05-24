package com.botaniq.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String = "db990a3a97d32b2d45dddb61aa9415b5",
        @Query("units") units: String = "metric", // Para obtener los grados como Celsius
        @Query("lang") lang: String = "es" // Para que la descripción salga en español
    ): WeatherResponse

    @GET("data/2.5/forecast")
    suspend fun getForecastWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String = "db990a3a97d32b2d45dddb61aa9415b5",
        @Query("units") units: String = "metric", // Para obtener los grados como Celsius
        @Query("lang") lang: String = "es" // Para que la descripción salga en español
    ): ForecastResponse
}

// 3. Configuración del Cliente Singleton (Retrofit Builder)
object RetrofitInstance {

    // Interceptor para comprobar las peticiones en el logcat
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // Inicialización lazy del cliente Retrofit
    val api: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}