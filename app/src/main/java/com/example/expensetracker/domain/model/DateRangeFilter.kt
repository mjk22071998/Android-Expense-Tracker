package com.example.expensetracker.domain.model

sealed class DateRangeFilter {
    data object Today : DateRangeFilter()
    data object ThisWeek : DateRangeFilter()
    data object ThisMonth : DateRangeFilter()
    data object ThisYear : DateRangeFilter()
    data class Custom(val startDate: Long, val endDate: Long) : DateRangeFilter()
}
