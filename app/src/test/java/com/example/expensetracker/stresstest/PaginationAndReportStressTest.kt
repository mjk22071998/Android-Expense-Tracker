package com.example.expensetracker.stresstest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.ceil

class PaginationAndReportStressTest {

    private val PAGE_1_ROWS_CAPACITY = 25
    private val SUBSEQUENT_PAGE_ROWS_CAPACITY = 30

    private fun calculateTotalPages(transactionCount: Int): Int {
        if (transactionCount <= PAGE_1_ROWS_CAPACITY) return 1
        val remaining = transactionCount - PAGE_1_ROWS_CAPACITY
        return 1 + ceil(remaining.toDouble() / SUBSEQUENT_PAGE_ROWS_CAPACITY).toInt()
    }

    @Test
    fun `calculateTotalPages produces exact page counts across critical capacity boundaries`() {
        assertEquals(1, calculateTotalPages(0))
        assertEquals(1, calculateTotalPages(1))
        assertEquals(1, calculateTotalPages(24))
        assertEquals(1, calculateTotalPages(25)) // Page 1 full

        assertEquals(2, calculateTotalPages(26)) // 1 row on Page 2
        assertEquals(2, calculateTotalPages(54))
        assertEquals(2, calculateTotalPages(55)) // Page 2 full

        assertEquals(3, calculateTotalPages(56)) // 1 row on Page 3
        assertEquals(3, calculateTotalPages(85)) // Page 3 full

        assertEquals(4, calculateTotalPages(86)) // 1 row on Page 4

        assertEquals(34, calculateTotalPages(1000)) // Large multi-page statement
        assertEquals(334, calculateTotalPages(10000)) // Massive 10k statement
    }

    @Test
    fun `pagination slicing algorithm guarantees zero duplicates, zero skips, and zero index errors for 10,000 items`() {
        val totalTransactions = 10000
        val totalPages = calculateTotalPages(totalTransactions)

        var currentTransactionIndex = 0
        val visitedIndices = mutableListOf<Int>()

        for (pageNumber in 1..totalPages) {
            val rowsForThisPage = if (pageNumber == 1) PAGE_1_ROWS_CAPACITY else SUBSEQUENT_PAGE_ROWS_CAPACITY
            val endTransactionIndex = (currentTransactionIndex + rowsForThisPage).coerceAtMost(totalTransactions)

            for (i in currentTransactionIndex until endTransactionIndex) {
                visitedIndices.add(i)
            }

            currentTransactionIndex = endTransactionIndex
        }

        // All 10,000 items must have been processed
        assertEquals(totalTransactions, currentTransactionIndex)
        assertEquals(totalTransactions, visitedIndices.size)
        // Verify perfect sequence 0, 1, 2, ..., 9999
        for (i in 0 until totalTransactions) {
            assertEquals(i, visitedIndices[i])
        }
    }

    @Test
    fun `empty transaction list generates exactly 1 page with no table rows`() {
        val totalPages = calculateTotalPages(0)
        assertEquals(1, totalPages)

        var currentTransactionIndex = 0
        val visitedIndices = mutableListOf<Int>()

        val rowsForThisPage = PAGE_1_ROWS_CAPACITY
        val endTransactionIndex = (currentTransactionIndex + rowsForThisPage).coerceAtMost(0)

        for (i in currentTransactionIndex until endTransactionIndex) {
            visitedIndices.add(i)
        }

        assertTrue(visitedIndices.isEmpty())
        assertEquals(0, endTransactionIndex)
    }
}
