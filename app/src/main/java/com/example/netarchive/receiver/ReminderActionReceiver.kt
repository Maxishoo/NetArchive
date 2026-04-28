package com.example.netarchive.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.netarchive.worker.DeleteReminderWorker
import com.example.netarchive.worker.ReminderNotificationWorker

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderActionReceiver", "🔔 onReceive! Action: ${intent.action}")

        val reminderId = intent.getIntExtra(ReminderNotificationWorker.KEY_REMINDER_ID, -1)
        Log.d("ReminderActionReceiver", "🔢 reminderId from intent: $reminderId")

        if (reminderId > 0) {
            Log.d("ReminderActionReceiver", "📦 Enqueuing DeleteReminderWorker...")

            val workRequest = OneTimeWorkRequestBuilder<DeleteReminderWorker>()
                .setInputData(workDataOf(ReminderNotificationWorker.KEY_REMINDER_ID to reminderId))
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            Log.d("ReminderActionReceiver", "✅ WorkManager enqueue called")
        } else {
            Log.e("ReminderActionReceiver", "❌ Invalid reminderId: $reminderId")
        }
    }
}