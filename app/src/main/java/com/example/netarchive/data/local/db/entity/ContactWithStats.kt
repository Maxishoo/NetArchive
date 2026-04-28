package com.example.netarchive.data.local.db.entity

import androidx.room.Embedded
import androidx.room.ColumnInfo

data class ContactWithStats(
    @Embedded val contact: ContactEntity,
    @ColumnInfo(name = "noteCount")
    val noteCount: Int = 0,
    @ColumnInfo(name = "lastNoteDate")
    val lastNoteDate: Long? = null
)
