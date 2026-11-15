package com.example.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)

    @Query("Select * from categories")
    fun getAllCategories(): Flow<List<Category>>

    @Query("Select * from categories where isDefault=0")
    suspend fun getNonDefaultCategories(): List<Category>

    @Update
    suspend fun update(category: Category)

    @Query("DELETE FROM categories WHERE id = :id AND isDefault = 0")
    suspend fun delete(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: Category)

    @Query("SELECT * FROM categories WHERE syncStatus != 0")
    suspend fun getPendingSyncCategories(): List<Category>

    @Query("UPDATE categories SET syncStatus = 0, serverId = :serverId, updatedAt = :verifiedUpdatedAt WHERE id = :id AND updatedAt = :verifiedUpdatedAt")
    suspend fun markAsSynced(id: String, serverId: String, verifiedUpdatedAt: Long): Int

    @Query("UPDATE categories SET syncStatus = 1")
    suspend fun markAllAsPendingUpload(): Int
}