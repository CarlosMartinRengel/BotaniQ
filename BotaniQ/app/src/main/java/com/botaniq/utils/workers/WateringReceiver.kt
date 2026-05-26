package com.botaniq.utils.workers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.botaniq.data.UserPreferenceRepository
import com.botaniq.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WateringReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Comprobar que es la acción correcta
        if (intent.action == "com.botaniq.ACTION_WATER_PLANT") {
            val plantId = intent.getIntExtra("PLANT_ID", -1)

            if (plantId != -1) {
                // Permite que el sistema se pause unos segundos para poder obtener la informacion
                val pendingResult = goAsync()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val appContext = context.applicationContext
                        val db = AppModule.provideDatabase(appContext, this)
                        val preferenceRepository = UserPreferenceRepository(appContext)
                        val currentCity = preferenceRepository.selectedCityFlow.first()
                        val repository = AppModule.providePlantRepository(
                            context = appContext,
                            plantDao = AppModule.providePlantDao(db),
                            speciesInfoDao = AppModule.provideSpeciesInfoDao(db),
                            weatherCacheDao = AppModule.provideWeatherCacheDao(db),
                            weatherApi = AppModule.provideWeatherApi()
                        )

                        val plant = repository.getPlantById(plantId)
                        if (plant != null) {
                            repository.confirmWatering(plant, currentCity)
                            Log.d(
                                "WateringReceiver",
                                "¡Planta ${plant.nickname} regada desde la notificación!"
                            )
                        }

                        val notificationManager =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(plantId)

                    } catch (e: Exception) {
                        Log.e("WateringReceiver", "Error al confirmar riego en segundo plano", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
