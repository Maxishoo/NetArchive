package com.example.netarchive.data.local.db.dao

import androidx.room.*
import com.example.netarchive.data.local.db.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE contactId = :contactId ORDER BY date DESC")
    fun getRemindersByContactId(contactId: Int): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getReminderById(reminderId: Int): ReminderEntity?

    @Query("SELECT * FROM reminders ORDER BY date DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE date >= :today")
    fun getAllFutureReminders(today: Long): Flow<List<ReminderEntity>>
}