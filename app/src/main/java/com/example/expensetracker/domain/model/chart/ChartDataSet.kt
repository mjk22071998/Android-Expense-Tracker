package com.example.expensetracker.domain.model.chart

data class ChartDataSet(
    val points: List<ChartDataPoint>,
    val periodType: ChartPeriodType,
    val totalExpense: Double,
    val totalIncome: Double,
    val maxExpense: Double,
    val maxIncome: Double,
    val maxBalance: Double,
    val minBalance: Double
) {
    val isEmpty: Boolean get() = points.isEmpty() || (totalExpense == 0.0 && totalIncome == 0.0 && points.all { it.transactionCount == 0 })

    companion object {
        fun empty(): ChartDataSet = ChartDataSet(
            points = emptyList(),
            periodType = ChartPeriodType.DAILY,
            totalExpense = 0.0,
            totalIncome = 0.0,
            maxExpense = 0.0,
            maxIncome = 0.0,
            maxBalance = 0.0,
            minBalance = 0.0
        )
    }
}
