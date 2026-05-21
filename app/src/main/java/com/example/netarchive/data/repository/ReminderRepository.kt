package com.example.netarchive.data.repository

import com.example.netarchive.data.mapper.toDomain
import com.example.netarchive.data.mapper.toEntity
import com.example.netarchive.domain.model.Reminder
import com.example.netarchive.domain.model.ReminderContact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepository @Inject constructor(
    private val reminderDao: com.example.netarchive.data.local.db.dao.ReminderDao
) {


    suspend fun addReminder(reminder: Reminder): Long {
        val insertedId = reminderDao.insertReminder(reminder.toEntity())
        return insertedId
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder.toEntity())
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder.toEntity())
    }

    fun getRemindersByContactId(contactId: Int): Flow<List<Reminder>> {
        return reminderDao.getRemindersByContactId(contactId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun getReminderById(reminderId: Int): Reminder? {
        return reminderDao.getReminderById(reminderId)?.toDomain()
    }

    fun getAllReminders(): Flow<List<Reminder>> {
        return reminderDao.getAllReminders()
            .map { entities -> entities.map { it.toDomain() } }
    }
    fun getAllFutureReminders(today: Long): Flow<List<Reminder>> {
        return reminderDao.getAllFutureReminders(today)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getRemindersWithContact(): Flow<List<ReminderContact>> {
        return reminderDao.getRemindersWithContact()
    }

     suspend fun deleteRemindersByIds(ids: List<Int>): Int {
        return reminderDao.deleteRemindersByIds(ids)
    }

    suspend fun getRemindersByIds(ids: List<Int>): List<Reminder> {
        return reminderDao.getRemindersByIds(ids).map { it.toDomain() }
    }

    suspend fun updateGoogleCalendarEventId(reminderId: Int, eventId: String?) {
        reminderDao.updateGoogleCalendarEventId(reminderId, eventId)
    }
}

