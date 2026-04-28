package com.example.netarchive.data.local.db.entity


data class ContactForReminder(
    val id: Int?,
    val username: String?,
    val phone: String?,
    val telegram: String?,
    val max: String?,
    val email: String?,
    val job: String?,
    val avatar: String?,
    val createdAt: Long?,
    val pinnedOrder: Int?,
    val birthday: Long?,
    val description: String?
)