package com.example.netarchive.domain.model

data class Contact(
    val id: Int = 0,
    val username : String,
    val phone: String? = null,
    val telegram : String? = null,
    val max : String? = null,
    val email : String? = null,
    val job : String? = null,
    val avatar : String? = null,
    val pinnedOrder: Int = 0,
    val birthday: Long? = null,
    val description: String? = null
)