package com.example.netarchive.data.mapper

import com.example.netarchive.data.local.db.entity.NoteEntity
import com.example.netarchive.domain.model.Note

fun NoteEntity.toDomain(): Note {
    return Note(
        id = this.id,
        contactId = this.contactId,
        text = this.text,
        date = this.date
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = this.id,
        contactId = this.contactId,
        text = this.text,
        date = this.date
    )
}