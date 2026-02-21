package com.example.netarchive.domain.model

data class Contact(
    val id: Int = 0,
    val username : String,
    val phone: String?,
    val telegram : String?,
    val max : String?,
    val email : String?,
    val job : String?,
    val avatar : String?
)