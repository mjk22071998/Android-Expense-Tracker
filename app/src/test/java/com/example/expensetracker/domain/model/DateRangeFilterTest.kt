package com.example.expensetracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateRangeFilterTest {

    @Test
    fun customFilter_returnsExactStartAndEnd() {
        val filter = DateRangeFilter.Custom(startDate = 1000L, endDate = 2000L)
        val (start, end) = filter.calculateRange()

        assertEquals(1000L, start)
        assertEquals(2000L, end)
    }

    @Test
    fun strategyImplementations_produceValidOrderedRanges() {
        val now = 1731050000000L // arbitrary fixed timestamp

        listOf(
            DateRangeFilter.Today,
            DateRangeFilter.ThisWeek,
            DateRangeFilter.ThisMonth,
            DateRangeFilter.ThisYear
        ).forEach { strategy ->
            val (start, end) = strategy.calculateRange(now)
            assertTrue("Start must be <= end for $strategy", start <= end)
            assertTrue("Now must be >= start for $strategy", now >= start)
            assertTrue("Now must be <= end for $strategy", now <= end)
        }
    }
}
