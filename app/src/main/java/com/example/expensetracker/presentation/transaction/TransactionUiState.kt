package com.example.expensetracker.presentation.transaction

import com.example.expensetracker.data.local.entity.Category

data class TransactionUiState(
    val currentLedgerId: String = "",
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
