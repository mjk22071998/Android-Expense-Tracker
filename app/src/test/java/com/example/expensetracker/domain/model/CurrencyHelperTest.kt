package com.example.expensetracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyHelperTest {

    @Test
    fun testCurrencySymbols() {
        assertEquals("Rs", CurrencyHelper.getSymbol("PKR"))
        assertEquals("$", CurrencyHelper.getSymbol("USD"))
        assertEquals("₹", CurrencyHelper.getSymbol("INR"))
        assertEquals("€", CurrencyHelper.getSymbol("EUR"))
        assertEquals("£", CurrencyHelper.getSymbol("GBP"))
        assertEquals("$", CurrencyHelper.getSymbol("CAD"))
        assertEquals("$", CurrencyHelper.getSymbol("AUD"))
        assertEquals("¥", CurrencyHelper.getSymbol("JPY"))
    }

    @Test
    fun testFormatCleanAmount() {
        assertEquals("40,000", CurrencyHelper.formatCleanAmount(40000.0))
        assertEquals("0", CurrencyHelper.formatCleanAmount(0.0))
        assertEquals("25,000.50", CurrencyHelper.formatCleanAmount(25000.50))
    }
}
