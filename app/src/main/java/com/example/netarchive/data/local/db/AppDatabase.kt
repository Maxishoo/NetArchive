package com.example.netarchive.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.netarchive.data.local.db.dao.CategoryDao
import com.example.netarchive.data.local.db.dao.ContactDao
import com.example.netarchive.data.local.db.dao.NoteDao
import com.example.netarchive.data.local.db.dao.ReminderDao
import com.example.netarchive.data.local.db.dao.ProfileDao

import com.example.netarchive.data.local.db.entity.CategoryEntity
import com.example.netarchive.data.local.db.entity.ContactCategoryCrossRef
import com.example.netarchive.data.local.db.entity.ContactEntity
import com.example.netarchive.data.local.db.entity.NoteEntity

import com.example.netarchive.data.local.db.entity.ProfileEntity
import com.example.netarchive.data.local.db.entity.ReminderEntity

//повысила версию после добавления списка напоминаний была 6-я
@Database(
    entities = [(ContactEntity::class), (NoteEntity::class), (ReminderEntity::class), (CategoryEntity::class), (ContactCategoryCrossRef::class), (ProfileEntity::class)],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun noteDao(): NoteDao

    abstract fun categoryDao(): CategoryDao


    abstract fun reminderDao(): ReminderDao

    abstract fun profileDao(): ProfileDao

    // AppDatabase.kt
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}