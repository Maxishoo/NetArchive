package com.example.netarchive.data.repository

import com.example.netarchive.data.local.db.dao.ContactDao
import com.example.netarchive.data.mapper.toDomain
import com.example.netarchive.data.mapper.toEntity
import com.example.netarchive.domain.model.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContactRepository(
    private val contactDao: ContactDao
){
    suspend fun addContact(contact : Contact){
        contactDao.addContact(contact.toEntity())
    }

    suspend fun updateContact(contact: Contact){
        contactDao.updateContact(contact.toEntity())
    }

    suspend fun deleteContact(contact: Contact){
        contactDao.deleteContact(contact.toEntity())
    }

    fun getContactById(id: Int): Flow<Contact?> {
        return contactDao.getContactById(id)
            .map { it?.toDomain() }
    }

    fun getAllContacts() : Flow<List<Contact>> {
        return contactDao.getAllContacts()
            .map { entities -> entities.map { it.toDomain() } }
    }
}