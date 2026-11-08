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

    // ---------- period ranges (local time, whole periods, inclusive on both ends) ----------

    fun todayLocalRange(now: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val start = cal.toStartOfDay().timeInMillis
        val end = cal.toEndOfDay().timeInMillis
        return start to end
    }

    /** Monday 00:00:00.000 through Sunday 23:59:59.999, regardless of the device's week-start setting. */
    fun thisWeekRange(now: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val daysSinceMonday = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
        cal.add(Calendar.DAY_OF_YEAR, -daysSinceMonday)
        val start = cal.toStartOfDay().timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 6)
        val end = cal.toEndOfDay().timeInMillis
        return start to end
    }

    fun thisMonthRange(now: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val start = cal.toStartOfDay().timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = cal.toEndOfDay().timeInMillis
        return start to end
    }

    fun lastMonthRange(now: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        cal.add(Calendar.MONTH, -1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val start = cal.toStartOfDay().timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = cal.toEndOfDay().timeInMillis
        return start to end
    }

    fun thisYearRange(now: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val start = cal.toStartOfDay().timeInMillis
        cal.set(Calendar.MONTH, Calendar.DECEMBER)
        cal.set(Calendar.DAY_OF_MONTH, 31)
        val end = cal.toEndOfDay().timeInMillis
        return start to end
    }

    // ---------- private helpers ----------

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
}