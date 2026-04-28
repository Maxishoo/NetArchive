package com.example.netarchive.utils

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.netarchive.worker.ReminderNotificationWorker
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    fun scheduleReminder(
        context: Context,
        reminderId: Int,
        title: String,
        text: String,
        timestamp: Long
    ) {
        val now = System.currentTimeMillis()
        val delay = timestamp - now
        if (delay <= 0) {
            return
        }
        val workRequest = OneTimeWorkRequestBuilder<ReminderNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ReminderNotificationWorker.KEY_TITLE to title,
                    ReminderNotificationWorker.KEY_TEXT to text,
                    ReminderNotificationWorker.KEY_REMINDER_ID to reminderId
                )
            )
            .addTag("reminder_$reminderId")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun cancelReminder(context: Context, reminderId: Int) {
        WorkManager.getInstance(context).cancelAllWorkByTag("reminder_$reminderId")
    }
}