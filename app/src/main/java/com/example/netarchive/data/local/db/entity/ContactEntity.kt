package com.example.netarchive.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val username : String,

    val phone: String? = null,

    val telegram : String? = null,

    val max : String? = null,

    val email : String? = null,

    val job : String? = null,

    val avatar : String? = null,

)