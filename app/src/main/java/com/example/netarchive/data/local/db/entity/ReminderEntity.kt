package com.example.netarchive.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val contactId: Int,
    val text: String,
    val date: Long,
    val isCompleted: Boolean = false
)