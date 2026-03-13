package com.example.netarchive.data.repository

import com.example.netarchive.data.local.db.dao.ContactDao
import com.example.netarchive.data.local.db.entity.ContactCategoryCrossRef
import com.example.netarchive.data.local.db.entity.ContactWithCategories
import com.example.netarchive.data.mapper.toDomain
import com.example.netarchive.data.mapper.toEntity
import com.example.netarchive.domain.model.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ContactRepository @Inject constructor(
    private val contactDao: ContactDao
) {
    suspend fun addContact(contact: Contact):Int {
        val entity = contact.toEntity()
        return contactDao.insertContact(entity).toInt()
    }

    suspend fun updateContact(contact: Contact) {
        contactDao.updateContact(contact.toEntity())
    }

    suspend fun deleteContact(contact: Contact) {
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
    suspend fun addCategoryToContact(contactId: Int, categoryId: Int) {
        contactDao.insertContactCategoryCrossRef(
            ContactCategoryCrossRef(contactId, categoryId)
        )
    }

    suspend fun updateContactCategories(contactId: Int, categoryIds: List<Int>) {
        // Удаляем все старые связи
        contactDao.deleteAllCategoriesForContact(contactId)

        // Добавляем новые
        categoryIds.forEach { categoryId ->
            contactDao.insertContactCategoryCrossRef(
                ContactCategoryCrossRef(contactId, categoryId)
            )
        }
    }
    suspend fun removeCategoryFromContact(contactId: Int, categoryId: Int) {  // <-- Int
        contactDao.deleteContactCategoryCrossRef(
            ContactCategoryCrossRef(contactId, categoryId)
        )
    }
    fun getContactWithCategories(contactId: Int): Flow<ContactWithCategories?> {
        return contactDao.getContactWithCategories(contactId)
    }
    fun getContactsByQueryAndCategory(query: String, categoryId: Int?): Flow<List<Contact>> {
        return contactDao.getContactsByQueryAndCategory(query, categoryId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    fun getContactsWithCategoriesByQueryAndCategory(
        query: String,
        categoryId: Int?
    ): Flow<List<ContactWithCategories>> {
        return contactDao.getContactsByQueryAndCategoryWithCategories(query, categoryId)
    }
}