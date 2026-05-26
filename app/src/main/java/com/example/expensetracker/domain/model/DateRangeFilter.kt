package com.example.expensetracker.domain.model

sealed class DateRangeFilter {
    object Last30Days : DateRangeFilter()
    object ThisWeek : DateRangeFilter()
    object ThisMonth : DateRangeFilter()
    object ThisYear : DateRangeFilter()
    data class Custom(val startDate: Long, val endDate: Long) : DateRangeFilter()
}