package com.example.expensetracker.presentation.dashboard

import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.domain.model.DateRangeFilter
import com.example.expensetracker.domain.model.UiError
import com.example.expensetracker.domain.model.chart.ChartDataSet
import com.example.expensetracker.domain.model.chart.ChartMode

data class CategoryUsage(
    val transactionCount: Int = 0,
    val totalAmount: Double = 0.0,
    val lastTransactionDate: Long? = null
)

data class DashboardUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val transactions: List<TransactionWithCategory> = emptyList(),
    val selectedFilter: DateRangeFilter = DateRangeFilter.ThisMonth,
    val currencySymbol: String = "Rs",
    val isLoading: Boolean = false,
    val error: UiError? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedTypeFilter: String? = null,
    val allCategories: List<Category> = emptyList(),
    val selectedCategoryIds: Set<String> = emptySet(),
    val isCategoryFilterSheetVisible: Boolean = false,
    val monthlyTrendDelta: Double? = null,
    val hasLastMonthData: Boolean = false,
    val thisMonthNet: Double = 0.0,
    val chartDataSet: ChartDataSet = ChartDataSet.empty(),
    val selectedChartMode: ChartMode = ChartMode.EXPENSES,
    val categoryUsageMap: Map<String, CategoryUsage> = emptyMap()
)
