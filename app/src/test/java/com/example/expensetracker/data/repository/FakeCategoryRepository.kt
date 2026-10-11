package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.domain.model.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCategoryRepository : CategoryRepository {

    private val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())

    fun emitCategories(categories: List<Category>) {
        categoriesFlow.value = categories
    }

    override fun getAllCategories(): Flow<List<Category>> = categoriesFlow

    override suspend fun insertCategory(category: Category): AppResult<Unit> =
        TODO("Not needed for current DashboardViewModel tests")

    override suspend fun updateCategory(category: Category): AppResult<Unit> =
        TODO("Not needed for current DashboardViewModel tests")

    override suspend fun deleteCategory(id: String): AppResult<Unit> =
        TODO("Not needed for current DashboardViewModel tests")
}