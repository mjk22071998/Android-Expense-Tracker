package com.example.expensetracker.domain.model.chart

data class ChartDataPoint(
    val label: String,             // Short label for X-axis (e.g. "Mon", "15", "Jan")
    val fullLabel: String,         // Extended label for tooltip (e.g. "Wednesday, Oct 14", "October 2026")
    val expense: Double,           // 0.0 if no expense that day
    val income: Double,            // 0.0 if no income that day
    val runningBalance: Double,    // Carried forward from previous day's balance if no activity
    val transactionCount: Int,     // Number of transactions in this bucket
    val startMillis: Long,
    val endMillis: Long
) {
    val net: Double get() = income - expense
}
