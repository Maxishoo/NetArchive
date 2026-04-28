package com.example.netarchive.data.repository

import com.example.netarchive.data.local.db.dao.CategoryDao
import com.example.netarchive.data.local.db.dao.CategoryWithCount
import com.example.netarchive.data.local.db.dao.ContactDao
import com.example.netarchive.data.local.db.dao.MonthActivity
import com.example.netarchive.data.mapper.toDomain
import com.example.netarchive.domain.model.Contact
import com.example.netarchive.domain.model.OverallStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepository @Inject constructor(
    private val contactDao: ContactDao,
    private val categoryDao: CategoryDao
) {

    fun getOverallStats(): Flow<OverallStats> {
        return combine(
            contactDao.getTotalContactsCount(),
            contactDao.getNewContactsThisMonth(),
            contactDao.getActiveContactsLastMonth()
        ) { total,new, active ->
            OverallStats(
                totalContacts = total,
                newContactsThisMonth = new,
                activeContactsPercent = if (total > 0) (active.toFloat() / total) * 100 else 0f
            )
        }
    }


    fun getMostActiveContacts(limit: Int = 5): Flow<List<Contact>> {
        return contactDao.getMostActiveContacts(limit)
            .map { entities -> entities.map { it.contact.toDomain() } }
    }


    fun getContactsToWrite(): Flow<List<Contact>> {
        return contactDao.getContactsToWrite()
            .map { entities -> entities.map { it.contact.toDomain() } }
    }

    fun getForgottenContacts(months: Int = 6): Flow<List<Contact>> {
        return contactDao.getForgottenContacts(months)
            .map { entities -> entities.map { it.contact.toDomain() } }
    }


    fun getCategoriesWithCount(): Flow<List<CategoryWithCount>> {
        return categoryDao.getCategoriesWithContactCount()
    }

    fun getMonthlyActivity(): Flow<List<MonthActivity>> {
        return contactDao.getNotesCountByMonth()
    }
}