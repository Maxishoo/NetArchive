package com.example.netarchive.data.repository

import com.example.netarchive.data.mapper.toDomain
import com.example.netarchive.data.mapper.toEntity
import com.example.netarchive.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: com.example.netarchive.data.local.db.dao.NoteDao
) {

    suspend fun addNote(note: Note) {
        noteDao.insertNote(note.toEntity())
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.toEntity())
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note.toEntity())
    }

    fun getNotesByContactId(contactId: Int): Flow<List<Note>> {
        return noteDao.getNotesByContactId(contactId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun getNoteById(noteId: Int): Note? {
        return noteDao.getNoteById(noteId)?.toDomain()
    }

    fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes()
            .map { entities -> entities.map { it.toDomain() } }
    }
}