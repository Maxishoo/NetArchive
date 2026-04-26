package com.example.netarchive.domain.model

import androidx.room.Embedded

data class ReminderContact(
    @Embedded val reminder: Reminder,
    @Embedded(prefix = "contact_") val contact: Contact?
)