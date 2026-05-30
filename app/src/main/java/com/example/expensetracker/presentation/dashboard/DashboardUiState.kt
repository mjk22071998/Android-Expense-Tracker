package com.example.expensetracker.presentation.dashboard

import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.domain.model.DateRangeFilter

data class DashboardUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val openingBalance: Double = 0.0,
    val closingBalance: Double = 0.0,
    val transactions: List<TransactionWithCategory> = emptyList(), // ← fix this
    val selectedFilter: DateRangeFilter = DateRangeFilter.Last30Days,
    val currencySymbol: String = "₨",
    val isLoading: Boolean = false,
    val error: String? = null
)
