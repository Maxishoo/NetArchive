package com.example.netarchive.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.netarchive.data.local.db.AppDatabase

// ❌ Убираем @HiltWorker и @AssistedInject — используем обычный конструктор
class DeleteReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val reminderId = inputData.getInt(ReminderNotificationWorker.KEY_REMINDER_ID, -1)
        Log.d("DeleteReminderWorker", "🗑️ Starting delete for reminderId=$reminderId")

        return try {
            // ✅ Прямой доступ к БД через синглтон
            val database = AppDatabase.getInstance(applicationContext)
            val deleted = database.reminderDao().deleteRemindersByIds(listOf(reminderId))

            Log.d("DeleteReminderWorker", "✅ Deleted $deleted reminder(s) with ID=$reminderId")
            if (deleted > 0) Result.success() else Result.failure()
        } catch (e: Exception) {
            Log.e("DeleteReminderWorker", "💥 Error", e)
            Result.failure()
        }
    }
}