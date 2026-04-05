package com.example.netarchive.domain.model

data class OverallStats(
    val totalContacts: Int,
    val newContactsThisMonth: Int,
    val activeContactsPercent: Float
)

data class ContactWithLastContact(
    val contact: Contact,
    val lastContactDate: Long?
)
