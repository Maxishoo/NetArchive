package com.example.netarchive.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val categoryRepository: CategoryRepository
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun initializeIfNeeded() {
        val isInitialized = prefs.getBoolean("categories_initialized", false)

        if (!isInitialized) {
            CoroutineScope(Dispatchers.IO).launch {
                categoryRepository.createDefaultCategories()
                prefs.edit().putBoolean("categories_initialized", true).apply()
            }
        }

    }
    fun cleanupUnusedCategories() {
        val lastCleanup = prefs.getLong("last_categories_cleanup", 0)
        val now = System.currentTimeMillis()
        val weekInMillis = 7 * 24 * 60 * 60 * 1000L  // 1 неделя

        if (lastCleanup > 0 && now - lastCleanup > weekInMillis) {
            CoroutineScope(Dispatchers.IO).launch {
                categoryRepository.deleteUnusedCustomCategories()
                prefs.edit().putLong("last_categories_cleanup", now).apply()
            }
        }
    }
}