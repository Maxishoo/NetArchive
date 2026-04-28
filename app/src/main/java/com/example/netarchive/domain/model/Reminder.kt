package com.example.netarchive.domain.model

data class Reminder(
    val id: Int = 0,
    val contactId: Int,
    val text: String,
    val date: Long,
    val isCompleted: Boolean = false
)