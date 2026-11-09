package com.example.expensetracker.stresstest

import com.example.expensetracker.domain.model.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DateBoundaryStressTest {

    @Test
    fun `thisMonthRange correctly determines month boundaries for every single day in a leap year`() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024) // 2024 is a leap year
            set(Calendar.DAY_OF_YEAR, 1)
        }

        // Test every day of the 366-day leap year
        for (day in 1..366) {
            val now = cal.timeInMillis
            val (start, end) = DateUtils.thisMonthRange(now)

            val checkCal = Calendar.getInstance().apply { timeInMillis = start }
            assertEquals(1, checkCal.get(Calendar.DAY_OF_MONTH))
            assertEquals(0, checkCal.get(Calendar.HOUR_OF_DAY))
            assertEquals(0, checkCal.get(Calendar.MINUTE))
            assertEquals(0, checkCal.get(Calendar.SECOND))
            assertEquals(0, checkCal.get(Calendar.MILLISECOND))

            checkCal.timeInMillis = end
            val expectedMaxDay = checkCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            assertEquals(expectedMaxDay, checkCal.get(Calendar.DAY_OF_MONTH))
            assertEquals(23, checkCal.get(Calendar.HOUR_OF_DAY))
            assertEquals(59, checkCal.get(Calendar.MINUTE))
            assertEquals(59, checkCal.get(Calendar.SECOND))
            assertEquals(999, checkCal.get(Calendar.MILLISECOND))

            assertTrue(start < end)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    @Test
    fun `lastMonthRange correctly handles end-of-month dates like March 31 without drifting`() {
        // March 31, 2024 (leap year) -> last month must be February 2024 (29 days)
        val march31Leap = Calendar.getInstance().apply {
            set(2024, Calendar.MARCH, 31, 15, 30, 0)
        }.timeInMillis

        val (startLeap, endLeap) = DateUtils.lastMonthRange(march31Leap)
        val calStartLeap = Calendar.getInstance().apply { timeInMillis = startLeap }
        val calEndLeap = Calendar.getInstance().apply { timeInMillis = endLeap }

        assertEquals(Calendar.FEBRUARY, calStartLeap.get(Calendar.MONTH))
        assertEquals(1, calStartLeap.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.FEBRUARY, calEndLeap.get(Calendar.MONTH))
        assertEquals(29, calEndLeap.get(Calendar.DAY_OF_MONTH))

        // March 31, 2025 (non-leap year) -> last month must be February 2025 (28 days)
        val march31NonLeap = Calendar.getInstance().apply {
            set(2025, Calendar.MARCH, 31, 15, 30, 0)
        }.timeInMillis

        val (startNonLeap, endNonLeap) = DateUtils.lastMonthRange(march31NonLeap)
        val calStartNonLeap = Calendar.getInstance().apply { timeInMillis = startNonLeap }
        val calEndNonLeap = Calendar.getInstance().apply { timeInMillis = endNonLeap }

        assertEquals(Calendar.FEBRUARY, calStartNonLeap.get(Calendar.MONTH))
        assertEquals(1, calStartNonLeap.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.FEBRUARY, calEndNonLeap.get(Calendar.MONTH))
        assertEquals(28, calEndNonLeap.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `lastMonthRange in January correctly rolls back to December of prior year`() {
        val jan15 = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 15, 10, 0, 0)
        }.timeInMillis

        val (start, end) = DateUtils.lastMonthRange(jan15)
        val calStart = Calendar.getInstance().apply { timeInMillis = start }
        val calEnd = Calendar.getInstance().apply { timeInMillis = end }

        assertEquals(2025, calStart.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, calStart.get(Calendar.MONTH))
        assertEquals(1, calStart.get(Calendar.DAY_OF_MONTH))

        assertEquals(2025, calEnd.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, calEnd.get(Calendar.MONTH))
        assertEquals(31, calEnd.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `thisWeekRange spans exactly 7 days from Monday 00h00 to Sunday 23h59 for every week of the year`() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.DAY_OF_YEAR, 1)
        }

        // Test every week across 52 weeks
        for (w in 1..52) {
            val now = cal.timeInMillis
            val (start, end) = DateUtils.thisWeekRange(now)

            val checkStart = Calendar.getInstance().apply { timeInMillis = start }
            val checkEnd = Calendar.getInstance().apply { timeInMillis = end }

            assertEquals("Start of week must be Monday", Calendar.MONDAY, checkStart.get(Calendar.DAY_OF_WEEK))
            assertEquals(0, checkStart.get(Calendar.HOUR_OF_DAY))
            assertEquals(0, checkStart.get(Calendar.MINUTE))
            assertEquals(0, checkStart.get(Calendar.SECOND))

            assertEquals("End of week must be Sunday", Calendar.SUNDAY, checkEnd.get(Calendar.DAY_OF_WEEK))
            assertEquals(23, checkEnd.get(Calendar.HOUR_OF_DAY))
            assertEquals(59, checkEnd.get(Calendar.MINUTE))
            assertEquals(59, checkEnd.get(Calendar.SECOND))

            val durationMs = end - start + 1
            val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
            assertEquals(sevenDaysMs, durationMs)

            cal.add(Calendar.DAY_OF_YEAR, 7)
        }
    }

    @Test
    fun `utcMidnight roundtrip preserves exact calendar day across diverse global timezones`() {
        val testZones = listOf(
            "UTC",
            "Asia/Karachi",
            "Asia/Tokyo",
            "America/New_York",
            "Europe/London",
            "Pacific/Honolulu",
            "Pacific/Kiritimati"
        )

        val originalZone = TimeZone.getDefault()
        try {
            for (zoneId in testZones) {
                TimeZone.setDefault(TimeZone.getTimeZone(zoneId))

                val testCal = Calendar.getInstance().apply {
                    set(2026, Calendar.JUNE, 15, 14, 25, 30)
                }

                val localMillis = testCal.timeInMillis
                val utcMidnight = DateUtils.localMillisToUtcMidnight(localMillis)
                val reconstructedLocalStart = DateUtils.utcMidnightToLocalStartOfDay(utcMidnight)

                val resultCal = Calendar.getInstance().apply { timeInMillis = reconstructedLocalStart }
                assertEquals("Year must match in $zoneId", 2026, resultCal.get(Calendar.YEAR))
                assertEquals("Month must match in $zoneId", Calendar.JUNE, resultCal.get(Calendar.MONTH))
                assertEquals("Day must match in $zoneId", 15, resultCal.get(Calendar.DAY_OF_MONTH))
                assertEquals("Hour must be 0 in $zoneId", 0, resultCal.get(Calendar.HOUR_OF_DAY))
            }
        } finally {
            TimeZone.setDefault(originalZone)
        }
    }
}
