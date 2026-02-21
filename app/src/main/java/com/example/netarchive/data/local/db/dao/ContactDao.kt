package com.example.netarchive.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.netarchive.data.local.db.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao{
    @Insert
    suspend fun addContact(contact: ContactEntity)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("SELECT * FROM Contacts WHERE id = :id")
    fun getContactById(id: Int): Flow<ContactEntity?>

    @Query("SELECT * FROM Contacts")
    fun getAllContacts() : Flow<List<ContactEntity>>
}