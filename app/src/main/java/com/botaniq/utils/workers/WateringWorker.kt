package com.botaniq.utils.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.botaniq.MainActivity
import com.botaniq.R


class WateringWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("TEST_WORKER", "1. ¡El sistema operativo ha despertado al Worker!")
        val plantName = inputData.getString("PLANT_NAME") ?: "Tu planta" //TODO literal
        val plantId = inputData.getInt("PLANT_ID", 0)

        try {
            showNotification(plantName, plantId)
            Log.d("TEST_WORKER", "2. Notificación construida y enviada al NotificationManager")
            return Result.success()
        } catch (e: Exception) {
            Log.e("TEST_WORKER", "Error fatal al mostrar la notificación", e)
            return Result.failure()
        }
    }

    private fun showNotification(plantName: String, plantId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "watering_channel"

        val channel = NotificationChannel(
            channelId,
            "Recordatorio de riego",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificacion para recordar el riego de plantas" // TODO literal
        }
        notificationManager.createNotificationChannel(channel)

        // La app se abre al clicar en el centro de la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            plantId, // ID único para el intent
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Si se clica en el texto, se riega la planta
        val actionIntent = Intent(context, WateringReceiver::class.java).apply {
            action = "com.botaniq.ACTION_WATER_PLANT"
            putExtra("PLANT_ID", plantId)
        }
        val actionPendingIntent = PendingIntent.getBroadcast(
            context,
            plantId,
            actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO Cambiar icono
            .setContentTitle("¡Hora de regar!") // TODO literal
            .setContentText("Tu $plantName necesita agua según nuestro análisis.") // TODO literal
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                0, // TODO icono y literal en planta regada
                "Planta regada",
                actionPendingIntent
            )
            .build()

        notificationManager.notify(plantId, notification)
    }
}