package com.example.expensetracker.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidatorTest {

    @Test
    fun emptyPassword_failsAllRequirements() {
        val result = PasswordValidator.validate("")
        assertFalse(result.hasMinLength)
        assertFalse(result.hasUppercase)
        assertFalse(result.hasLowercase)
        assertFalse(result.hasDigit)
        assertFalse(result.hasSymbol)
        assertFalse(result.isValid)
        assertEquals(0, result.satisfiedCount)
    }

    @Test
    fun shortPasswordWithMixedChars_failsMinLengthOnly() {
        val result = PasswordValidator.validate("Aa1!b")
        assertFalse(result.hasMinLength)
        assertTrue(result.hasUppercase)
        assertTrue(result.hasLowercase)
        assertTrue(result.hasDigit)
        assertTrue(result.hasSymbol)
        assertFalse(result.isValid)
        assertEquals(4, result.satisfiedCount)
    }

    @Test
    fun longPasswordMissingUppercase_failsUppercaseOnly() {
        val result = PasswordValidator.validate("lowercase123!@#")
        assertTrue(result.hasMinLength)
        assertFalse(result.hasUppercase)
        assertTrue(result.hasLowercase)
        assertTrue(result.hasDigit)
        assertTrue(result.hasSymbol)
        assertFalse(result.isValid)
        assertEquals(4, result.satisfiedCount)
    }

    @Test
    fun longPasswordMissingLowercase_failsLowercaseOnly() {
        val result = PasswordValidator.validate("UPPERCASE123!@#")
        assertTrue(result.hasMinLength)
        assertTrue(result.hasUppercase)
        assertFalse(result.hasLowercase)
        assertTrue(result.hasDigit)
        assertTrue(result.hasSymbol)
        assertFalse(result.isValid)
        assertEquals(4, result.satisfiedCount)
    }

    @Test
    fun longPasswordMissingDigit_failsDigitOnly() {
        val result = PasswordValidator.validate("Password!@#")
        assertTrue(result.hasMinLength)
        assertTrue(result.hasUppercase)
        assertTrue(result.hasLowercase)
        assertFalse(result.hasDigit)
        assertTrue(result.hasSymbol)
        assertFalse(result.isValid)
        assertEquals(4, result.satisfiedCount)
    }

    @Test
    fun longPasswordMissingSymbol_failsSymbolOnly() {
        val result = PasswordValidator.validate("Password1234")
        assertTrue(result.hasMinLength)
        assertTrue(result.hasUppercase)
        assertTrue(result.hasLowercase)
        assertTrue(result.hasDigit)
        assertFalse(result.hasSymbol)
        assertFalse(result.isValid)
        assertEquals(4, result.satisfiedCount)
    }

    @Test
    fun strongPassword_satisfiesAllFiveRequirements() {
        val result = PasswordValidator.validate("ValidP@ssw0rd!")
        assertTrue(result.hasMinLength)
        assertTrue(result.hasUppercase)
        assertTrue(result.hasLowercase)
        assertTrue(result.hasDigit)
        assertTrue(result.hasSymbol)
        assertTrue(result.isValid)
        assertEquals(5, result.satisfiedCount)
        assertEquals(5, result.totalCount)
    }
}
