package com.example.netarchive.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.netarchive.data.local.db.dao.ContactDao
import com.example.netarchive.data.local.db.dao.NoteDao
import com.example.netarchive.data.local.db.entity.ContactEntity
import com.example.netarchive.data.local.db.entity.NoteEntity

@Database(entities = [(ContactEntity::class), (NoteEntity::class)], version = 2, exportSchema = false)
abstract class AppDatabase: RoomDatabase(){
    abstract fun contactDao() : ContactDao
    abstract fun noteDao(): NoteDao
}