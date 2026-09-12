package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.domain.model.AppResult
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun insertCategory(category: Category): AppResult<Unit>
    suspend fun updateCategory(category: Category): AppResult<Unit>
    suspend fun deleteCategory(id: String): AppResult<Unit>
}