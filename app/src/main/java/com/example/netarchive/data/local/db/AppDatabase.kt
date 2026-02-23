package com.example.netarchive.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.netarchive.data.local.db.dao.ContactDao
import com.example.netarchive.data.local.db.entity.ContactEntity

@Database(entities = [(ContactEntity::class)], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase(){
    abstract fun contactDao() : ContactDao
}