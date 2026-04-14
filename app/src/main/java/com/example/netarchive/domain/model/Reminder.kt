package com.example.netarchive.domain.model

data class Reminder(
    val id: Int = 0,
    val contactId: Int,
    val text: String,
    val timestamp: Long,
    val isCompleted: Boolean = false
)