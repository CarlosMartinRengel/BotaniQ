package com.botaniq.utils.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.botaniq.MainActivity
import com.botaniq.R

class WateringWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("TEST_WORKER", "1. El sistema ha activado el Worker")

        val plantName = inputData.getString("PLANT_NAME")
            ?: context.getString(R.string.worker_default_plant_name)

        val plantId = inputData.getInt("PLANT_ID", 0)

        try {
            showNotification(plantName, plantId)
            Log.d("TEST_WORKER", "2. Notificación creada y enviada")
            return Result.success()
        } catch (e: Exception) {
            Log.e("TEST_WORKER", "Error al mostrar la notificación", e)
            return Result.failure()
        }
    }

    private fun showNotification(plantName: String, plantId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "watering_channel"

        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
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
            .setSmallIcon(R.drawable.botaniq_stat_watering)
            .setColor(ContextCompat.getColor(context, R.color.botaniq_launcher_background))
            .setContentTitle(context.getString(R.string.notification_watering_title))
            .setContentText(context.getString(R.string.notification_watering_body, plantName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                0,
                context.getString(R.string.notification_action_watered),
                actionPendingIntent
            )
            .build()

        notificationManager.notify(plantId, notification)
    }
}