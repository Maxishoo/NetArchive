package com.example.netarchive.data.local.db.dao

import androidx.room.*
import com.example.netarchive.data.local.db.entity.ReminderEntity
import com.example.netarchive.domain.model.ReminderContact
import kotlinx.coroutines.flow.Flow


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


    @Transaction
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
        contacts.avatar AS contact_avatar,
        contacts.pinnedOrder AS contact_pinnedOrder,      
        contacts.birthday AS contact_birthday,            
        contacts.description AS contact_description 
    FROM reminders
    INNER JOIN contacts ON reminders.contactId = contacts.id  
    ORDER BY reminders.date DESC
""")
    fun getRemindersWithContact(): Flow<List<ReminderContact>>



    @Query("DELETE FROM reminders WHERE id IN (:ids)")
    suspend fun deleteRemindersByIds(ids: List<Int>): Int

    @Query("SELECT * FROM reminders WHERE id IN (:ids)")
    suspend fun getRemindersByIds(ids: List<Int>): List<ReminderEntity>

    @Query("UPDATE reminders SET googleCalendarEventId = :eventId WHERE id = :reminderId")
    suspend fun updateGoogleCalendarEventId(reminderId: Int, eventId: String?)

    @Query("""
        SELECT reminders.id AS reminderId,
               reminders.text AS reminderText,
               reminders.date AS reminderDate,
               reminders.contactId AS contactId,
               contacts.username AS contactName
        FROM reminders
        INNER JOIN contacts ON reminders.contactId = contacts.id
        WHERE reminders.date >= :fromMillis
        ORDER BY reminders.date ASC
        LIMIT :limit
    """)
    suspend fun getUpcomingRemindersForWidget(
        fromMillis: Long,
        limit: Int,
    ): List<WidgetReminderRow>

}

data class WidgetReminderRow(
    val reminderId: Int,
    val reminderText: String,
    val reminderDate: Long,
    val contactId: Int,
    val contactName: String,
)