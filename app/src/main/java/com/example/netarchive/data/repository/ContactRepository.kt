package com.example.netarchive.data.repository

import android.net.Uri
import com.example.netarchive.data.local.db.dao.ContactDao
import com.example.netarchive.data.mapper.toDomain
import com.example.netarchive.data.mapper.toEntity
import com.example.netarchive.domain.model.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import androidx.core.net.toUri

class ContactRepository @Inject constructor(
    private val contactDao: ContactDao
) {
    suspend fun addContact(contact: Contact) {
        contactDao.addContact(contact.toEntity())
    }

    suspend fun updateContact(contact: Contact) {
        contactDao.updateContact(contact.toEntity())
    }

    suspend fun deleteContact(contact: Contact) {
        contact.avatar?.let {
            File(it.toUri().path!!).delete()
        }
        contactDao.deleteContact(contact.toEntity())
    }

    fun getContactById(id: Int): Flow<Contact?> {
        return contactDao.getContactById(id)
            .map { it?.toDomain() }
    }

    fun getAllContacts(): Flow<List<Contact>> {
        return contactDao.getAllContacts()
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun getContacts(query: String): Flow<List<Contact>> {
        return contactDao.getContacts(query)
            .map { entities -> entities.map { it.toDomain() } }
    }
}