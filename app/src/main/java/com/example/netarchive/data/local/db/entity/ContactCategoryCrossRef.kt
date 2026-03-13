package com.example.netarchive.data.local.db.entity

import androidx.room.Entity

@Entity(
    tableName = "contact_category_cross_ref",
    primaryKeys = ["contactId", "categoryId"]
)
data class ContactCategoryCrossRef(
    val contactId: Int,
    val categoryId: Int
)

