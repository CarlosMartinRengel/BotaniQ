package com.botaniq.utils.workers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.botaniq.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WateringReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Comprobamos que es la acción correcta
        if (intent.action == "com.botaniq.ACTION_WATER_PLANT") {
            val plantId = intent.getIntExtra("PLANT_ID", -1)

            if (plantId != -1) {
                // goAsync() le dice a Android: "Dame unos segundos más, voy a hacer trabajo en segundo plano"
                val pendingResult = goAsync()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // 1. Inyección manual de dependencias
                        val appContext = context.applicationContext
                        val db = AppModule.provideDatabase(appContext, this)
                        val repository = AppModule.providePlantRepository(
                            context = appContext,
                            plantDao = AppModule.providePlantDao(db),
                            speciesInfoDao = AppModule.provideSpeciesInfoDao(db),
                            weatherCacheDao = AppModule.provideWeatherCacheDao(db),
                            weatherApi = AppModule.provideWeatherApi()
                        )

                        // 2. Obtener la planta y calcular el nuevo riego
                        val plant = repository.getPlantById(plantId)
                        if (plant != null) {
                            repository.confirmWatering(plant, "Salamanca") // TODO: Ciudad dinámica
                            Log.d(
                                "WateringReceiver",
                                "¡Planta ${plant.nickname} regada desde la notificación!"
                            )
                        }

                        // 3. Ocultar (cancelar) la notificación
                        val notificationManager =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(plantId)

                    } catch (e: Exception) {
                        Log.e("WateringReceiver", "Error al confirmar riego en segundo plano", e)
                    } finally {
                        // Avisar siempre del fin
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
