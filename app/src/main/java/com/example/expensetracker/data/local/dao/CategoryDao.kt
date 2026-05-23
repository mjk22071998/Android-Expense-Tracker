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

    @Update
    suspend fun update(category: Category)

    @Query("Select * from categories")
    suspend fun getAllCategories(): Flow<List<Category>>

    @Query("Select * from categories where isDefault=0")
    suspend fun getNonDefaultCategories(): List<Category>

    @Query("delete from categories where id=:id and isDefault=0")
    suspend fun delete(id: String)
}