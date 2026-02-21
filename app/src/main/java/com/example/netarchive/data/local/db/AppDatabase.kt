package com.example.netarchive.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.netarchive.data.local.db.dao.ContactDao
import com.example.netarchive.data.local.db.entity.ContactEntity

@Database(entities = [(ContactEntity::class)], version = 2)
abstract class AppDatabase: RoomDatabase(){
    abstract fun ContactDao() : ContactDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {

            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                        context.applicationContext,
                                        AppDatabase::class.java,
                                        "User_database"

                                    ).fallbackToDestructiveMigration(false).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}