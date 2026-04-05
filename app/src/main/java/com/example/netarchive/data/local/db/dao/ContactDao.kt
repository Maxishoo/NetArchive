package com.example.netarchive.data.local.db.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.netarchive.data.local.db.entity.ContactEntity
import com.example.netarchive.data.local.db.entity.ContactCategoryCrossRef
import com.example.netarchive.data.local.db.entity.ContactWithCategories
import com.example.netarchive.data.local.db.entity.ContactWithStats
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao{
    @Insert
    suspend fun addContact(contact: ContactEntity):Long

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("SELECT * FROM Contacts WHERE id = :id")
    fun getContactById(id: Int): Flow<ContactEntity?>

    @Query("SELECT * FROM Contacts")
    fun getAllContacts() : Flow<List<ContactEntity>>

    @Query("SELECT * FROM Contacts " +
            "WHERE " +
            "username LIKE '%'||:query||'%'" +
            "OR phone LIKE '%'||:query||'%'" +
            "OR job LIKE '%'||:query||'%'")
    fun getContacts(query : String) : Flow<List<ContactEntity>>

    @Transaction
    @Query("SELECT * FROM contacts WHERE id = :contactId")
    fun getContactWithCategories(contactId: Int): Flow<ContactWithCategories?>

    @Transaction
    @Query("SELECT * FROM contacts")
    fun getAllContactsWithCategories(): Flow<List<ContactWithCategories>>

    @Transaction
    @Query("""
    SELECT * FROM contacts 
    WHERE id IN (
        SELECT contactId FROM contact_category_cross_ref 
        WHERE categoryId = :categoryId
    )
""")
    fun getContactsByCategory(categoryId: Int): Flow<List<ContactWithCategories>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContactCategoryCrossRef(ref: ContactCategoryCrossRef):Long

    @Delete
    suspend fun deleteContactCategoryCrossRef(ref: ContactCategoryCrossRef)

    @Query("DELETE FROM contact_category_cross_ref WHERE contactId = :contactId")
    suspend fun deleteAllCategoriesForContact(contactId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Transaction
    @Query("""
    SELECT * FROM contacts 
    WHERE 
        (username LIKE '%' || :query || '%' 
        OR phone LIKE '%' || :query || '%' 
        OR job LIKE '%' || :query || '%'
        OR id IN (
            SELECT contactId FROM contact_category_cross_ref 
            WHERE categoryId IN (
                SELECT id FROM categories 
                WHERE name LIKE '%' || :query || '%'
            )
        ))
        AND (:categoryId IS NULL OR id IN (
            SELECT contactId FROM contact_category_cross_ref 
            WHERE categoryId = :categoryId
        ))
    ORDER BY username ASC
""")
    fun getContactsByQueryAndCategory(query: String, categoryId: Int?): Flow<List<ContactEntity>>

    @Transaction
    @Query("""
    SELECT * FROM contacts 
    WHERE 
        (username LIKE '%' || :query || '%' 
        OR phone LIKE '%' || :query || '%' 
        OR job LIKE '%' || :query || '%'
        OR id IN (
            SELECT contactId FROM contact_category_cross_ref 
            WHERE categoryId IN (
                SELECT id FROM categories 
                WHERE name LIKE '%' || :query || '%'
            )
        ))
        AND (:categoryId IS NULL OR id IN (
            SELECT contactId FROM contact_category_cross_ref 
            WHERE categoryId = :categoryId
        ))
    ORDER BY username ASC
""")
    fun getContactsByQueryAndCategoryWithCategories(
        query: String,
        categoryId: Int?
    ): Flow<List<ContactWithCategories>>

    @Query("""
    SELECT c.*, COUNT(n.id) as noteCount, MAX(n.date) as lastNoteDate
    FROM contacts c
    LEFT JOIN notes n ON c.id = n.contactId 
        AND n.date > (strftime('%s', 'now') * 1000 - 90 * 24 * 60 * 60 * 1000)
    GROUP BY c.id
    HAVING noteCount > 0
    ORDER BY noteCount DESC, lastNoteDate DESC
    LIMIT :limit
""")
    fun getMostActiveContacts(limit: Int = 5): Flow<List<ContactWithStats>>

    @Query("""
    SELECT c.*, 
           COUNT(n.id) as noteCount,  
           MAX(n.date) as lastNoteDate 
    FROM contacts c
    LEFT JOIN notes n ON c.id = n.contactId
    GROUP BY c.id
    HAVING lastNoteDate IS NULL 
        OR lastNoteDate < (strftime('%s', 'now') * 1000 - 14 * 24 * 60 * 60 * 1000)
    ORDER BY 
        CASE WHEN lastNoteDate IS NULL THEN 0 ELSE 1 END,
        lastNoteDate ASC
""")
    fun getContactsToWrite(): Flow<List<ContactWithStats>>

    @Query("""
    SELECT c.*, COUNT(n.id) as noteCount, MAX(n.date) as lastNoteDate 
    FROM contacts c
    LEFT JOIN notes n ON c.id = n.contactId
    GROUP BY c.id
    HAVING lastNoteDate IS NULL 
        OR lastNoteDate < (strftime('%s', 'now') * 1000 - :days * 24 * 60 * 60 * 1000)
    ORDER BY 
        CASE WHEN lastNoteDate IS NULL THEN 0 ELSE 1 END,
        lastNoteDate ASC
""")
    fun getForgottenContacts(days: Int = 180): Flow<List<ContactWithStats>>  // ← меняем months на days

    @Query("SELECT COUNT(*) FROM contacts")
    fun getTotalContactsCount(): Flow<Int>

    @Query("""
    SELECT COUNT(DISTINCT contactId) 
    FROM notes 
    WHERE date > (strftime('%s', 'now') * 1000 - 30 * 24 * 60 * 60 * 1000)
""")
    fun getActiveContactsLastMonth(): Flow<Int>

    @Query("""
    SELECT 
        strftime('%Y-%m', date/1000, 'unixepoch') as month, 
        COUNT(*) as count
    FROM notes
    GROUP BY month
    ORDER BY month DESC
    LIMIT 6
""")
    fun getNotesCountByMonth(): Flow<List<MonthActivity>>

    @Query("""
    SELECT COUNT(*) 
    FROM contacts 
    WHERE createdAt > (strftime('%s', 'now') * 1000 - 30 * 24 * 60 * 60 * 1000)
""")
    fun getNewContactsThisMonth(): Flow<Int>


}
data class MonthActivity(
    @ColumnInfo(name = "month")
    val month: String,  // например "2026-04"
    @ColumnInfo(name = "count")
    val count: Int
)