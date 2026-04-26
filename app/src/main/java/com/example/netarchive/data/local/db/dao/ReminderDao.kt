package com.example.netarchive.data.local.db.dao

import androidx.room.*
import com.example.netarchive.data.local.db.entity.ReminderEntity
import com.example.netarchive.domain.model.ReminderContact
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity) : Long

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

    @Query("""
    SELECT 
        reminders.*,
        contacts.id AS contact_id,
        contacts.username AS contact_username,
        contacts.phone AS contact_phone,
        contacts.telegram AS contact_telegram,
        contacts.max AS contact_max,
        contacts.email AS contact_email,
        contacts.job AS contact_job,
        contacts.avatar AS contact_avatar
    FROM reminders
    LEFT JOIN contacts ON reminders.contactId = contacts.id
    ORDER BY reminders.date DESC
""")
    fun getRemindersWithContact(): Flow<List<ReminderContact>>



    @Query("DELETE FROM reminders WHERE id IN (:ids)")
    suspend fun deleteRemindersByIds(ids: List<Int>): Int // ← возвращает Int (кол-во удалённых)

    @Query("SELECT id FROM reminders")
    suspend fun debugGetAllIds(): List<Int>

    @Query("SELECT COUNT(*) FROM reminders")
    suspend fun countReminders(): Int

}