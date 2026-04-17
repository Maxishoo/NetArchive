package com.example.netarchive.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.netarchive.data.local.db.entity.ContactEntity
import com.example.netarchive.data.local.db.entity.ContactCategoryCrossRef
import com.example.netarchive.data.local.db.entity.ContactWithCategories
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

    @Query("DELETE FROM contacts")
    suspend fun clearContactsTable()

    @Query("SELECT phone FROM contacts")
    suspend fun getContactsPhones() : List<String>

    @Query("SELECT * FROM contacts WHERE id = :contactId")
    fun getContactWithCategories(contactId: Int): Flow<ContactWithCategories?>

    @Query("SELECT * FROM contacts")
    fun getAllContactsWithCategories(): Flow<List<ContactWithCategories>>

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
    ORDER BY
        CASE WHEN pinnedOrder > 0 THEN 0 ELSE 1 END,
        pinnedOrder ASC,
        username ASC
""")
    fun getContactsByQueryAndCategoryWithCategories(
        query: String,
        categoryId: Int?
    ): Flow<List<ContactWithCategories>>

    @Query("SELECT MAX(pinnedOrder) FROM contacts")
    suspend fun getMaxPinnedOrder(): Int?

    @Query("UPDATE contacts SET pinnedOrder = :order WHERE id = :contactId")
    suspend fun updatePinnedOrder(contactId: Int, order: Int)

    suspend fun pinContact(contactId: Int) {
        val maxOrder = getMaxPinnedOrder() ?: 0
        updatePinnedOrder(contactId, maxOrder + 1)
    }

    @Query("UPDATE contacts SET pinnedOrder = 0 WHERE id = :contactId")
    suspend fun unpinContact(contactId: Int)

    @Query("""
        UPDATE contacts 
        SET pinnedOrder = CASE 
            WHEN id = :contact1Id THEN (SELECT pinnedOrder FROM Contacts WHERE id = :contact2Id)
            WHEN id = :contact2Id THEN (SELECT pinnedOrder FROM Contacts WHERE id = :contact1Id)
        END
        WHERE id IN (:contact1Id, :contact2Id)
    """)
    suspend fun swapPinnedContacts(contact1Id: Int, contact2Id: Int)
}