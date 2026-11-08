package com.example.expensetracker.domain.model

/**
 * Strategy pattern for date range boundary calculation.
 * Encapsulates the period calculation strategy for each filter variant,
 * satisfying the Open/Closed Principle (OCP) and Single Responsibility Principle (SRP).
 */
sealed class DateRangeFilter {
    abstract fun calculateRange(now: Long = System.currentTimeMillis()): Pair<Long, Long>

    data object AllTime : DateRangeFilter() {
        override fun calculateRange(now: Long): Pair<Long, Long> = 0L to Long.MAX_VALUE
    }

    data object Today : DateRangeFilter() {
        override fun calculateRange(now: Long): Pair<Long, Long> = DateUtils.todayLocalRange(now)
    }

    data object ThisWeek : DateRangeFilter() {
        override fun calculateRange(now: Long): Pair<Long, Long> = DateUtils.thisWeekRange(now)
    }

    data object ThisMonth : DateRangeFilter() {
        override fun calculateRange(now: Long): Pair<Long, Long> = DateUtils.thisMonthRange(now)
    }

    data object ThisYear : DateRangeFilter() {
        override fun calculateRange(now: Long): Pair<Long, Long> = DateUtils.thisYearRange(now)
    }

    data class Custom(val startDate: Long, val endDate: Long) : DateRangeFilter() {
        override fun calculateRange(now: Long): Pair<Long, Long> = startDate to endDate
    }
}
