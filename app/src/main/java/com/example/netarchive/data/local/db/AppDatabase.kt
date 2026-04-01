package com.example.netarchive.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.netarchive.data.local.db.dao.CategoryDao
import com.example.netarchive.data.local.db.dao.ContactDao
import com.example.netarchive.data.local.db.dao.NoteDao
import com.example.netarchive.data.local.db.dao.ProfileDao
import com.example.netarchive.data.local.db.entity.CategoryEntity
import com.example.netarchive.data.local.db.entity.ContactCategoryCrossRef
import com.example.netarchive.data.local.db.entity.ContactEntity
import com.example.netarchive.data.local.db.entity.NoteEntity
import com.example.netarchive.data.local.db.entity.ProfileEntity

@Database(
    entities = [(ContactEntity::class), (NoteEntity::class), (CategoryEntity::class), (ContactCategoryCrossRef::class), (ProfileEntity::class)],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun noteDao(): NoteDao

    abstract fun categoryDao(): CategoryDao

    abstract fun profileDao(): ProfileDao
}