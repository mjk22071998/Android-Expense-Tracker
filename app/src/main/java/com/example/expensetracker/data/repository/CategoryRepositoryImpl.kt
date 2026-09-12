package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.dao.CategoryDao
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.domain.model.AppResult
import com.example.expensetracker.domain.model.safeCall
import jakarta.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {
    override fun getAllCategories() = categoryDao.getAllCategories()

    override suspend fun insertCategory(category: Category): AppResult<Unit> = safeCall {
        categoryDao.insert(category)
    }

    override suspend fun updateCategory(category: Category): AppResult<Unit> = safeCall {
        categoryDao.update(category)
    }

    override suspend fun deleteCategory(id: String): AppResult<Unit> = safeCall {
        categoryDao.delete(id)
    }
}