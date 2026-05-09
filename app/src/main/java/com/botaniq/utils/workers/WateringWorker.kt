package com.botaniq.utils.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
        val plantName = inputData.getString("PLANT_NAME") ?: "Tu planta" //TODO literal
        val plantId = inputData.getInt("PLANT_ID", 0)

        showNotification(plantName, plantId)

        return Result.success()
    }

    private fun showNotification(plantName: String, plantId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "watering_channel"

        // Comprobacion del sdk para crear el canal de notificaciones o no, a partir de Android 8 es obligatorio
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorio de riego",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificacion para recordar el riego de plantas" // TODO literal
            }
            notificationManager.createNotificationChannel(channel)
        }

        // La app se abre al clicar en la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            plantId, // ID único para el intent
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO Cambiar icono
            .setContentTitle("¡Hora de regar!") // TODO literal
            .setContentText("Tu $plantName necesita agua según nuestro análisis.") // TODO literal
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(plantId, notification)
    }
}