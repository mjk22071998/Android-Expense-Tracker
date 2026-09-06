package com.example.expensetracker.presentation.transaction

import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.data.local.entity.Transaction

data class AddEditTransactionUiState(
    val isEditMode: Boolean = false,
    val amount: String = "",
    val type: String = "EXPENSE",
    val selectedCategoryId: String? = null,
    val allCategories: List<Category> = emptyList(),
    val filteredCategories: List<Category> = emptyList(),
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val currencySymbol: String = "Rs.",
    val isSaveEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val originalTransaction: Transaction? = null,
    val currentLedgerId: String = ""
)
