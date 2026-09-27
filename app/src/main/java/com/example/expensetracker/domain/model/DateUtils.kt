package com.example.expensetracker.domain.model

import java.util.Calendar
import java.util.TimeZone

/**
 * Material3's DatePicker/DateRangePicker always represent a selected calendar day
 * as UTC midnight of that day, regardless of device time zone. These helpers bridge
 * that representation to real local-time instants — the ones used everywhere else
 * in the app for transaction timestamps and SQL date range boundaries.
 */
object DateUtils {

    fun todayAsUtcMidnight(): Long {
        val local = Calendar.getInstance()
        return localDatePartsToUtcMidnight(
            local.get(Calendar.YEAR),
            local.get(Calendar.MONTH),
            local.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun localMillisToUtcMidnight(localMillis: Long): Long {
        val local = Calendar.getInstance().apply { timeInMillis = localMillis }
        return localDatePartsToUtcMidnight(
            local.get(Calendar.YEAR),
            local.get(Calendar.MONTH),
            local.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun utcMidnightToLocalStartOfDay(utcMidnightMillis: Long): Long {
        val (year, month, day) = utcMidnightMillis.toUtcDateParts()
        return Calendar.getInstance().apply {
            clear()
            set(year, month, day, 0, 0, 0)
        }.timeInMillis
    }

    fun utcMidnightToLocalEndOfDay(utcMidnightMillis: Long): Long {
        val (year, month, day) = utcMidnightMillis.toUtcDateParts()
        return Calendar.getInstance().apply {
            clear()
            set(year, month, day, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private fun localDatePartsToUtcMidnight(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month, day, 0, 0, 0)
        }.timeInMillis
    }

    private fun Long.toUtcDateParts(): Triple<Int, Int, Int> {
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = this@toUtcDateParts
        }
        return Triple(
            utcCal.get(Calendar.YEAR),
            utcCal.get(Calendar.MONTH),
            utcCal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun todayLocalRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return start to end
    }
}