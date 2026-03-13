package com.example.netarchive.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.netarchive.data.local.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isDefault = 1 ORDER BY name ASC")
    fun getDefaultCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchCategories(query: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)


    @Query("""
    SELECT * FROM categories 
    WHERE isDefault = 0 
    AND id NOT IN (
        SELECT categoryId FROM contact_category_cross_ref
    )
""")
    fun getUnusedCustomCategories(): Flow<List<CategoryEntity>>

    @Query("""
    DELETE FROM categories 
    WHERE isDefault = 0 
    AND id NOT IN (
        SELECT categoryId FROM contact_category_cross_ref
    )
""")
    suspend fun deleteUnusedCustomCategories(): Int

    @Query("""
    SELECT c.* FROM categories c
    WHERE c.isDefault = 0 
    AND c.id NOT IN (
        SELECT categoryId FROM contact_category_cross_ref
    )
    ORDER BY c.name
""")
    suspend fun getUnusedCustomCategoriesList(): List<CategoryEntity>
}
