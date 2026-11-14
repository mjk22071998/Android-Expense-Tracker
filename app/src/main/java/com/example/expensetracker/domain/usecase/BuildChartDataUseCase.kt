package com.example.expensetracker.domain.usecase

import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.domain.model.DateRangeFilter
import com.example.expensetracker.domain.model.DateUtils
import com.example.expensetracker.domain.model.chart.ChartDataPoint
import com.example.expensetracker.domain.model.chart.ChartDataSet
import com.example.expensetracker.domain.model.chart.ChartPeriodType
import jakarta.inject.Inject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BuildChartDataUseCase @Inject constructor() {

    fun execute(
        transactions: List<TransactionWithCategory>,
        filter: DateRangeFilter,
        initialBalance: Double,
        now: Long = System.currentTimeMillis()
    ): ChartDataSet {
        val bins = generateBins(filter, now, transactions)
        if (bins.isEmpty()) return ChartDataSet.empty()

        // Filter out soft-deleted transactions just in case
        val activeTransactions = transactions.filter { !it.isDeleted }

        var currentBalance = initialBalance
        val points = ArrayList<ChartDataPoint>(bins.size)

        for (bin in bins) {
            val matching = activeTransactions.filter { tx ->
                tx.date in bin.startMillis..bin.endMillis
            }

            val expense = matching.filter { it.type == Constants.TransactionType.EXPENSE }
                .sumOf { it.amount }
            val income = matching.filter { it.type == Constants.TransactionType.INCOME }
                .sumOf { it.amount }

            // Zero-activity days: expense=0.0, income=0.0, balance carries forward flat!
            if (matching.isNotEmpty()) {
                currentBalance += (income - expense)
            }

            points.add(
                ChartDataPoint(
                    label = bin.label,
                    fullLabel = bin.fullLabel,
                    expense = expense,
                    income = income,
                    runningBalance = currentBalance,
                    transactionCount = matching.size,
                    startMillis = bin.startMillis,
                    endMillis = bin.endMillis
                )
            )
        }

        val totalExpense = points.sumOf { it.expense }
        val totalIncome = points.sumOf { it.income }
        val maxExpense = points.maxOfOrNull { it.expense } ?: 0.0
        val maxIncome = points.maxOfOrNull { it.income } ?: 0.0
        val maxBalance = points.maxOfOrNull { it.runningBalance } ?: initialBalance
        val minBalance = points.minOfOrNull { it.runningBalance } ?: initialBalance

        val periodType = if (filter is DateRangeFilter.ThisYear || (filter is DateRangeFilter.AllTime)) {
            ChartPeriodType.MONTHLY
        } else if (filter is DateRangeFilter.Custom && (filter.endDate - filter.startDate > 31L * 24 * 60 * 60 * 1000)) {
            ChartPeriodType.MONTHLY
        } else {
            ChartPeriodType.DAILY
        }

        return ChartDataSet(
            points = points,
            periodType = periodType,
            totalExpense = totalExpense,
            totalIncome = totalIncome,
            maxExpense = maxExpense,
            maxIncome = maxIncome,
            maxBalance = maxBalance,
            minBalance = minBalance
        )
    }

    private data class BinTemplate(
        val label: String,
        val fullLabel: String,
        val startMillis: Long,
        val endMillis: Long
    )

    private fun generateBins(
        filter: DateRangeFilter,
        now: Long,
        transactions: List<TransactionWithCategory>
    ): List<BinTemplate> {
        return when (filter) {
            is DateRangeFilter.ThisWeek -> generateWeekBins(now)
            is DateRangeFilter.ThisMonth -> generateMonthBins(now)
            is DateRangeFilter.ThisYear -> generateYearBins(now)
            is DateRangeFilter.Today -> generateTodayBins(now)
            is DateRangeFilter.Custom -> generateCustomBins(filter.startDate, filter.endDate, now)
            is DateRangeFilter.AllTime -> generateAllTimeBins(now, transactions)
        }
    }

    private fun generateWeekBins(now: Long): List<BinTemplate> {
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }
        val daysSinceMonday = (nowCal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
        val cal = (nowCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -daysSinceMonday)
        }

        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val fullFormatter = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val bins = ArrayList<BinTemplate>(daysSinceMonday + 1)

        for (i in 0..daysSinceMonday) {
            val startMillis = cal.toStartOfDay().timeInMillis
            val endMillis = cal.toEndOfDay().timeInMillis
            val fullLabel = fullFormatter.format(cal.time)
            bins.add(
                BinTemplate(
                    label = dayNames[i],
                    fullLabel = fullLabel,
                    startMillis = startMillis,
                    endMillis = endMillis
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return bins
    }

    private fun generateMonthBins(now: Long): List<BinTemplate> {
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }
        val currentDay = nowCal.get(Calendar.DAY_OF_MONTH)
        val year = nowCal.get(Calendar.YEAR)
        val month = nowCal.get(Calendar.MONTH)

        val cal = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
        }
        val fullFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val bins = ArrayList<BinTemplate>(currentDay)

        for (day in 1..currentDay) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val startMillis = cal.toStartOfDay().timeInMillis
            val endMillis = cal.toEndOfDay().timeInMillis
            val fullLabel = fullFormatter.format(cal.time)
            bins.add(
                BinTemplate(
                    label = day.toString(),
                    fullLabel = fullLabel,
                    startMillis = startMillis,
                    endMillis = endMillis
                )
            )
        }
        return bins
    }

    private fun generateYearBins(now: Long): List<BinTemplate> {
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }
        val year = nowCal.get(Calendar.YEAR)
        val currentMonth = nowCal.get(Calendar.MONTH)
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val fullFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val bins = ArrayList<BinTemplate>(currentMonth + 1)

        val cal = Calendar.getInstance()
        for (month in 0..currentMonth) {
            cal.clear()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val startMillis = cal.toStartOfDay().timeInMillis

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            val endMillis = cal.toEndOfDay().timeInMillis

            val fullLabel = fullFormatter.format(cal.time)
            bins.add(
                BinTemplate(
                    label = monthNames[month],
                    fullLabel = fullLabel,
                    startMillis = startMillis,
                    endMillis = endMillis
                )
            )
        }
        return bins
    }

    private fun generateTodayBins(now: Long): List<BinTemplate> {
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }
        val currentHour = nowCal.get(Calendar.HOUR_OF_DAY)
        val segments = listOf(
            Triple("Night", "Night (12 AM - 6 AM)", 0 to 5),
            Triple("Morn", "Morning (6 AM - 12 PM)", 6 to 11),
            Triple("After", "Afternoon (12 PM - 6 PM)", 12 to 17),
            Triple("Eve", "Evening (6 PM - 12 AM)", 18 to 23)
        )
        val activeSegments = segments.filter { it.third.first <= currentHour }
        val cal = (nowCal.clone() as Calendar)
        return (if (activeSegments.isEmpty()) listOf(segments.first()) else activeSegments).map { (shortLbl, fullLbl, hourRange) ->
            cal.set(Calendar.HOUR_OF_DAY, hourRange.first)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, hourRange.second)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val end = cal.timeInMillis

            BinTemplate(shortLbl, fullLbl, start, end)
        }
    }

    private fun generateCustomBins(startDate: Long, endDate: Long, now: Long): List<BinTemplate> {
        val cal = Calendar.getInstance()
        val effectiveEnd = minOf(endDate, now).coerceAtLeast(startDate)
        val durationDays = ((effectiveEnd - startDate) / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(1)

        return if (durationDays <= 31) {
            // Daily bins
            val bins = ArrayList<BinTemplate>()
            cal.timeInMillis = startDate
            val dayFormatter = SimpleDateFormat("d MMM", Locale.getDefault())
            val fullFormatter = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())

            while (cal.toStartOfDay().timeInMillis <= effectiveEnd) {
                val start = cal.toStartOfDay().timeInMillis
                val end = cal.toEndOfDay().timeInMillis
                bins.add(
                    BinTemplate(
                        label = dayFormatter.format(cal.time),
                        fullLabel = fullFormatter.format(cal.time),
                        startMillis = start,
                        endMillis = end
                    )
                )
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            bins
        } else {
            // Monthly bins
            val bins = ArrayList<BinTemplate>()
            cal.timeInMillis = startDate
            val monthFormatter = SimpleDateFormat("MMM yy", Locale.getDefault())
            val fullFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

            while (cal.toStartOfDay().timeInMillis <= effectiveEnd) {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = cal.toStartOfDay().timeInMillis

                val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, lastDay)
                val end = cal.toEndOfDay().timeInMillis

                bins.add(
                    BinTemplate(
                        label = monthFormatter.format(cal.time),
                        fullLabel = fullFormatter.format(cal.time),
                        startMillis = start,
                        endMillis = end
                    )
                )
                cal.add(Calendar.MONTH, 1)
            }
            bins
        }
    }

    private fun generateAllTimeBins(now: Long, transactions: List<TransactionWithCategory>): List<BinTemplate> {
        if (transactions.isEmpty()) {
            return generateYearBins(now)
        }
        val minDate = transactions.minOf { it.date }
        val maxDate = transactions.maxOf { it.date }.coerceAtLeast(now)
        return generateCustomBins(minDate, maxDate, now)
    }

    private fun Calendar.toStartOfDay(): Calendar = apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun Calendar.toEndOfDay(): Calendar = apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
}
