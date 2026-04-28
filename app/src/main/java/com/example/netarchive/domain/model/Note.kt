package com.example.netarchive.domain.model

data class Note(
    val id: Int = 0,
    val contactId: Int,
    val text: String,
    val date: Long = System.currentTimeMillis()
)
