package com.example.expensetracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun testJodaMoneyIntegration() {
        val usdMoney = CurrencyHelper.toMoney(125.50, "USD")
        assertEquals("USD", usdMoney.currencyUnit.code)
        assertEquals(2, usdMoney.currencyUnit.decimalPlaces)
        assertEquals(125.50, usdMoney.amount.toDouble(), 0.001)

        val jpyMoney = CurrencyHelper.toMoney(5000.0, "JPY")
        assertEquals("JPY", jpyMoney.currencyUnit.code)
        assertEquals(0, jpyMoney.currencyUnit.decimalPlaces)
        assertEquals(5000L, jpyMoney.amountMajorLong)

        assertTrue(CurrencyHelper.isValidCurrency("USD"))
        assertTrue(CurrencyHelper.isValidCurrency("EUR"))
        assertTrue(CurrencyHelper.isValidCurrency("PKR"))
        assertFalse(CurrencyHelper.isValidCurrency("INVALID_XYZ"))
    }
}
