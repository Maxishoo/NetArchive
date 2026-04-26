package com.example.netarchive.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.netarchive.receiver.ReminderActionReceiver
import com.example.netarchive.utils.NotificationHelper

class ReminderNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Напоминание"
        val text = inputData.getString(KEY_TEXT) ?: ""
        val reminderId = inputData.getInt(KEY_REMINDER_ID, 0)

        showNotification(title, text, reminderId)

        return Result.success()
    }

    private fun showNotification(title: String, text: String, reminderId: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        Log.d("ReminderNotif", "Creating notification for reminderId=$reminderId")

        // ✅ 1. Делаем Intent ЯВНЫМ (указываем класс получателя)
        val completeIntent = Intent(applicationContext, ReminderActionReceiver::class.java).apply {
            action = ACTION_COMPLETE
            putExtra(KEY_REMINDER_ID, reminderId)
        }

        // ✅ 2. Уникальный requestCode (чтобы PendingIntent не пересекались)
        val requestCode = reminderId + 10000

        // ✅ 3. ТОЛЬКО один флаг: FLAG_IMMUTABLE (без FLAG_MUTABLE!)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            requestCode,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, "Выполнено", pendingIntent)
            .build()

        notificationManager.notify(reminderId, notification)
        Log.d("ReminderNotif", "PendingIntent created: $pendingIntent")
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_TEXT = "text"
        const val KEY_REMINDER_ID = "reminderId"
        const val ACTION_COMPLETE = "com.example.netarchive.ACTION_COMPLETE_REMINDER"
    }
}