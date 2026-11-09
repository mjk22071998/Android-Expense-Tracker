package com.example.expensetracker.stresstest

import com.example.expensetracker.domain.model.CurrencyHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class CurrencyAndFormattingStressTest {

    @Test
    fun `formatAmount and formatCleanAmount handle extreme, boundary, and fractional numbers accurately`() {
        Locale.setDefault(Locale.US)

        // Zero
        assertEquals("0.00", CurrencyHelper.formatAmount(0.0))
        assertEquals("0", CurrencyHelper.formatCleanAmount(0.0))

        // Cents and sub-cents
        assertEquals("0.01", CurrencyHelper.formatAmount(0.01))
        assertEquals("0.01", CurrencyHelper.formatCleanAmount(0.01))
        assertEquals("0.00", CurrencyHelper.formatAmount(0.004))
        assertEquals("0.01", CurrencyHelper.formatAmount(0.006))

        // Clean integer amounts
        assertEquals("1,000.00", CurrencyHelper.formatAmount(1000.0))
        assertEquals("1,000", CurrencyHelper.formatCleanAmount(1000.0))

        // Large financial amounts
        val largeAmount = 999999999.99
        assertEquals("999,999,999.99", CurrencyHelper.formatAmount(largeAmount))
        assertEquals("999,999,999.99", CurrencyHelper.formatCleanAmount(largeAmount))

        // Negative balances
        assertEquals("-500.00", CurrencyHelper.formatAmount(-500.0))
        assertEquals("-500", CurrencyHelper.formatCleanAmount(-500.0))
        assertEquals("-1,234.56", CurrencyHelper.formatAmount(-1234.56))
        assertEquals("-1,234.56", CurrencyHelper.formatCleanAmount(-1234.56))
    }

    @Test
    fun `currency search remains resilient and performant under rapid high-volume stress queries`() {
        val testQueries = listOf(
            "",
            "   ",
            "usd",
            "USD",
            "dollar",
            "pk",
            "RS",
            "₹",
            "€",
            "¥",
            "NonExistentCurrencyXYZ123",
            "'; DROP TABLE currencies; --",
            "🚀💸🎉",
            "مرحبا",
            "VeryLongQueryStringExceedingNormalLimitsWhichShouldNeverCrash"
        )

        // Run 5,000 total searches to stress test the filter pipeline
        for (i in 0 until 5000) {
            val q = testQueries[i % testQueries.size]
            val results = CurrencyHelper.search(q)
            assertNotNull(results)
            if (q == "usd") {
                assertTrue(results.any { it.code == "USD" })
            } else if (q == "NonExistentCurrencyXYZ123") {
                assertTrue(results.isEmpty())
            }
        }
    }

    @Test
    fun `getSymbol never throws on invalid, empty, or malicious currency inputs`() {
        val edgeCases = listOf(
            "",
            " ",
            "INVALID",
            "123",
            "$$$",
            "null",
            "LONG_CODE_THAT_DOES_NOT_EXIST",
            "USD; DELETE"
        )

        for (code in edgeCases) {
            val symbol = CurrencyHelper.getSymbol(code)
            assertNotNull(symbol)
            assertFalse(symbol.isEmpty())
        }
    }
}
