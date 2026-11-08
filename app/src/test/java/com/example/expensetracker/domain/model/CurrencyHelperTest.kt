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
}
