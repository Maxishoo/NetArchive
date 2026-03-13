package com.example.netarchive.di

import android.content.Context
import androidx.room.Room
import com.example.netarchive.data.local.db.AppDatabase
import com.example.netarchive.data.local.db.dao.CategoryDao
import com.example.netarchive.data.local.db.dao.ContactDao
import com.example.netarchive.data.local.db.dao.NoteDao
import com.example.netarchive.data.repository.CategoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "archive.db"
        )
            .fallbackToDestructiveMigration(false)
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
    fun provideCategoryRepository(
        categoryDao: CategoryDao
    ): CategoryRepository {
        return CategoryRepository(categoryDao)
    }
}