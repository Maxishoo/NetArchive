package com.example.netarchive.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Notes",
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["contactId"])]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val contactId: Int,

    val text: String,

    val date: Long = System.currentTimeMillis()
)
