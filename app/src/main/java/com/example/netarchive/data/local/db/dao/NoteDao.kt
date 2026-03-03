package com.example.netarchive.data.local.db.dao

import androidx.room.*
import com.example.netarchive.data.local.db.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT * FROM Notes WHERE contactId = :contactId ORDER BY date DESC")
    fun getNotesByContactId(contactId: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM Notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): NoteEntity?

    @Query("SELECT * FROM Notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
}