package com.example.netarchive.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username : String,
    val phone: String? = null,
    val telegram : String? = null,
    val max : String? = null,
    val email : String? = null,
    val job : String? = null,
    val avatar : String? = null,
    val createdAt: Long = System.currentTimeMillis()

)
data class ContactWithCategories(
    @Embedded val contact: ContactEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ContactCategoryCrossRef::class,
            parentColumn = "contactId",
            entityColumn = "categoryId"
        )
    )
    val categories: List<CategoryEntity>
)