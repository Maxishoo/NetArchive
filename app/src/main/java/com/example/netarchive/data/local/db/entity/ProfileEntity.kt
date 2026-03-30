package com.example.netarchive.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val username : String,
    val phone: String? = null,
    val telegram : String? = null,
    val max : String? = null,
    val email : String? = null,
    val job : String? = null,
    val avatar : String? = null,
    )