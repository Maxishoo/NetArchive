package com.example.netarchive.di

import android.content.Context
import androidx.room.Room
import com.example.netarchive.data.local.db.AppDatabase
import com.example.netarchive.data.local.db.dao.*
import com.example.netarchive.data.repository.CategoryRepository
import com.example.netarchive.data.repository.ReminderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // ← Разрешено, т.к. есть @Module выше
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database" // ← Имя должно совпадать с тем, что в логах!
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides fun provideContactDao(db: AppDatabase): ContactDao = db.contactDao()
    @Provides fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()
    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideProfileDao(db: AppDatabase): ProfileDao = db.profileDao()
    @Provides fun provideReminderDao(db: AppDatabase): ReminderDao = db.reminderDao()
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides @Singleton
    fun provideCategoryRepository(categoryDao: CategoryDao): CategoryRepository = CategoryRepository(categoryDao)

    @Provides @Singleton
    fun provideReminderRepository(reminderDao: ReminderDao): ReminderRepository = ReminderRepository(reminderDao)
}