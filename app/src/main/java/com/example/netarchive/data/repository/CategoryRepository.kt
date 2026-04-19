package com.example.netarchive.data.repository

import com.example.netarchive.data.local.db.dao.CategoryDao
import com.example.netarchive.data.local.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class
CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    val defaultCategories: Flow<List<CategoryEntity>> = categoryDao.getDefaultCategories()

    fun searchCategories(query: String): Flow<List<CategoryEntity>> {
        return categoryDao.searchCategories(query)
    }

    suspend fun getCategoryById(categoryId: Int): CategoryEntity? {
        return categoryDao.getCategoryById(categoryId)
    }

    suspend fun createCategory(name: String, isDefault: Boolean = false): Int {
        val category = CategoryEntity(name = name, isDefault = isDefault)
        return categoryDao.insertCategory(category).toInt()
    }

    suspend fun createCategoryIfNotExists(name: String): Int {
        // Проверяем существует ли категория
        val existing = categoryDao.searchCategories(name)
            .firstOrNull()
            ?.find { it.name.equals(name, ignoreCase = true) }

        return existing?.id ?: run {
            // Создаём новую
            val category = CategoryEntity(name = name, isDefault = false)
            categoryDao.insertCategory(category).toInt()
        }
    }

    suspend fun createDefaultCategories() {
        val defaults = listOf(
            CategoryEntity(name = "Семья", isDefault = true),
            CategoryEntity(name = "Работа", isDefault = true),
            CategoryEntity(name = "Друзья", isDefault = true),
            CategoryEntity(name = "Коллеги", isDefault = true),
            CategoryEntity(name = "Знакомые", isDefault = true)
        )
        categoryDao.insertCategories(defaults)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    suspend fun getUnusedCustomCategories(): List<CategoryEntity> {
        return categoryDao.getUnusedCustomCategoriesList()
    }

    suspend fun deleteUnusedCustomCategories(): Int {
        val deleted = categoryDao.deleteUnusedCustomCategories()
        return deleted
    }

    suspend fun deleteCustomCategory(categoryId: Int) {
        val category = categoryDao.getCategoryById(categoryId)
        if (category != null && !category.isDefault) {
            categoryDao.deleteCategory(category)
        }
    }
}