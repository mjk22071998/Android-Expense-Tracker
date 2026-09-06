package com.example.expensetracker.data.local

import com.example.expensetracker.data.local.dao.CategoryDao
import com.example.expensetracker.data.local.entity.Category
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class CategorySeeder @Inject constructor(
    private val categoryDao: CategoryDao
) {
    suspend fun seedIfNeeded() {
        val existing = categoryDao.getNonDefaultCategories()
        // Actually check default categories specifically
        val defaultCategories = getDefaultCategoryList()
        defaultCategories.forEach { category ->
            categoryDao.insert(category) // IGNORE strategy — safe to call every launch
        }
    }

    private fun getDefaultCategoryList(): List<Category> {
        return listOf(
            Category(id = "cat_food", name = "Food", icon = "restaurant", transactionType = "EXPENSE", isDefault = true),
            Category(id = "cat_transport", name = "Transport", icon = "transport", transactionType = "EXPENSE", isDefault = true),
            Category(id = "cat_shopping", name = "Shopping", icon = "shopping", transactionType = "EXPENSE", isDefault = true),
            Category(id = "cat_health", name = "Health", icon = "health", transactionType = "EXPENSE", isDefault = true),
            Category(id = "cat_entertainment", name = "Entertainment", icon = "entertainment", transactionType = "EXPENSE", isDefault = true),
            Category(id = "cat_utilities", name = "Utilities", icon = "utilities", transactionType = "EXPENSE", isDefault = true),
            Category(id = "cat_rent", name = "Rent", icon = "rent", transactionType = "EXPENSE", isDefault = true),
            Category(id = "cat_other_expense", name = "Other", icon = "other", transactionType = "EXPENSE", isDefault = true),

            Category(id = "cat_salary", name = "Salary", icon = "salary", transactionType = "INCOME", isDefault = true),
            Category(id = "cat_investment", name = "Investment", icon = "investment", transactionType = "INCOME", isDefault = true),
            Category(id = "cat_other_income", name = "Other", icon = "other", transactionType = "INCOME", isDefault = true)
        )
    }
}