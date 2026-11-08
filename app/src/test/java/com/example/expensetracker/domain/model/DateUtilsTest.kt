package com.example.expensetracker.domain.model

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class DateUtilsTest {

    private lateinit var originalLocale: Locale
    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setup() {
        originalLocale = Locale.getDefault()
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Karachi"))
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
        TimeZone.setDefault(originalTimeZone)
    }

    private fun at(
        year: Int, month: Int, day: Int,
        hour: Int = 0, minute: Int = 0, second: Int = 0, millis: Int = 0
    ): Long = Calendar.getInstance().apply {
        clear()
        set(year, month, day, hour, minute, second)
        set(Calendar.MILLISECOND, millis)
    }.timeInMillis

    private fun dayOfWeek(time: Long): Int =
        Calendar.getInstance().apply { timeInMillis = time }.get(Calendar.DAY_OF_WEEK)

    // Sunday Nov 8 2026, 14:30:00.537 — the exact case that used to return a future start date
    private val sunday = at(2026, Calendar.NOVEMBER, 8, 14, 30, 0, 537)

    @Test
    fun `this week starts on Monday even on a Sunday in a Sunday-first locale`() {
        Locale.setDefault(Locale.US)

        val (start, end) = DateUtils.thisWeekRange(sunday)

        assertEquals(at(2026, Calendar.NOVEMBER, 2), start)
        assertEquals(Calendar.SUNDAY, dayOfWeek(end))
        assertTrue(sunday in start..end)
    }

    @Test
    fun `this week gives the same answer in a Monday-first locale`() {
        Locale.setDefault(Locale.UK)

        val (start, end) = DateUtils.thisWeekRange(sunday)

        assertEquals(at(2026, Calendar.NOVEMBER, 2), start)
        assertEquals(at(2026, Calendar.NOVEMBER, 8, 23, 59, 59, 999), end)
    }

    @Test
    fun `a transaction picked for the 1st at exact midnight is inside this month`() {
        val (start, end) = DateUtils.thisMonthRange(sunday)
        val pickedForFirst = at(2026, Calendar.NOVEMBER, 1) // what the date picker stores

        assertTrue(pickedForFirst in start..end)
    }

    @Test
    fun `this month ends on the last day at 23-59-59-999 including leap day`() {
        val (_, end) = DateUtils.thisMonthRange(at(2028, Calendar.FEBRUARY, 10, 12))

        assertEquals(at(2028, Calendar.FEBRUARY, 29, 23, 59, 59, 999), end)
    }

    @Test
    fun `this year runs from Jan 1 to Dec 31`() {
        val (start, end) = DateUtils.thisYearRange(sunday)

        assertEquals(at(2026, Calendar.JANUARY, 1), start)
        assertEquals(at(2026, Calendar.DECEMBER, 31, 23, 59, 59, 999), end)
    }

    @Test
    fun `today covers the whole day`() {
        val (start, end) = DateUtils.todayLocalRange(sunday)

        assertEquals(at(2026, Calendar.NOVEMBER, 8), start)
        assertEquals(at(2026, Calendar.NOVEMBER, 8, 23, 59, 59, 999), end)
    }

    @Test
    fun `last month runs from first day to last day of previous month`() {
        val (start, end) = DateUtils.lastMonthRange(sunday)

        assertEquals(at(2026, Calendar.OCTOBER, 1), start)
        assertEquals(at(2026, Calendar.OCTOBER, 31, 23, 59, 59, 999), end)
    }

    @Test
    fun `last month in January wraps to December of previous year`() {
        val (start, end) = DateUtils.lastMonthRange(at(2026, Calendar.JANUARY, 15))

        assertEquals(at(2025, Calendar.DECEMBER, 1), start)
        assertEquals(at(2025, Calendar.DECEMBER, 31, 23, 59, 59, 999), end)
    }

    @Test
    fun `last month in March of leap year ends on Feb 29`() {
        val (start, end) = DateUtils.lastMonthRange(at(2028, Calendar.MARCH, 15))

        assertEquals(at(2028, Calendar.FEBRUARY, 1), start)
        assertEquals(at(2028, Calendar.FEBRUARY, 29, 23, 59, 59, 999), end)
    }
}
