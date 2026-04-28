package com.example.netarchive.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.netarchive.data.local.db.AppDatabase
import com.example.netarchive.data.local.db.dao.CategoryDao
import com.example.netarchive.data.local.db.dao.ContactDao
import com.example.netarchive.data.local.db.dao.NoteDao
import com.example.netarchive.data.local.security.SecurityHelper
import com.example.netarchive.data.local.db.dao.ReminderDao
import com.example.netarchive.data.local.db.dao.ProfileDao
import com.example.netarchive.data.repository.CategoryRepository
import com.example.netarchive.data.repository.ReminderRepository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.zetetic.database.sqlcipher.SupportHelper



@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        val password = SecurityHelper.getDatabasePassword()

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "archive.db"
        )
            .openHelperFactory { config ->
                // 🔐 Просто создаём SupportHelper - библиотеки загрузятся сами
                net.zetetic.database.sqlcipher.SupportHelper(
                    config,
                    password,
                    null,
                    false
                )
            }
            .fallbackToDestructiveMigration()
            .build()
    }




    @Provides
    @Singleton
    fun provideContactDao(database: AppDatabase): ContactDao {
        return database.contactDao()
    }
    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }
    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideProfileDao(database: AppDatabase): ProfileDao{
        return database.profileDao()
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao
    ): CategoryRepository {
        return CategoryRepository(categoryDao)
    }


    @Module
    @InstallIn(SingletonComponent::class)
    object RepositoryModule {

        @Provides
        @Singleton
        fun provideReminderRepository(
            reminderDao: ReminderDao
        ): ReminderRepository = ReminderRepository(reminderDao)
    }
    @Provides
    @Singleton
    fun provideReminderDao(database: AppDatabase): ReminderDao =
        database.reminderDao()
}