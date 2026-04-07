package com.example.netarchive.data.mapper

import com.example.netarchive.data.local.db.entity.ReminderEntity
import com.example.netarchive.domain.model.Reminder

fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = id,
        contactId = contactId,
        text = text,
        timestamp = date,
        isCompleted = isCompleted
    )
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        contactId = contactId,
        text = text,
        date = timestamp,
        isCompleted = isCompleted
    )
}