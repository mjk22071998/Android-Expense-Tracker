package com.example.expensetracker.presentation.settings

import android.content.Context
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.UserPreferences
import com.example.expensetracker.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Backing flows we can push new values into from inside a test,
    // simulating DataStore emitting a fresh preference at any point.
    private val currencyCodeFlow = MutableStateFlow("PKR")
    private val themeModeFlow = MutableStateFlow(Constants.ThemeMode.SYSTEM)

    private lateinit var userPreferences: UserPreferences
    private lateinit var context: Context
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        userPreferences = mockk(relaxed = true) {
            every { currencyCode } returns currencyCodeFlow
            every { themeMode } returns themeModeFlow
        }
        viewModel = SettingsViewModel(userPreferences, context)
    }

    @Test
    fun `initial state reflects the currency code from UserPreferences`() {
        assertEquals("PKR", viewModel.uiState.value.currencyCode)
    }

    @Test
    fun `pushing a new value into the currency flow updates ui state`() {
        currencyCodeFlow.value = "USD"
        assertEquals("USD", viewModel.uiState.value.currencyCode)
    }

    @Test
    fun `search query filters the currency list to matches only`() {
        viewModel.onCurrencySearchQueryChanged("pound")

        val results = viewModel.uiState.value.filteredCurrencies

        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.name.contains("Pound", ignoreCase = true) })
    }

    @Test
    fun `selecting a currency saves it and closes the picker`() = runTest {
        viewModel.onCurrencyPickerVisibilityChanged(true)

        viewModel.onCurrencySelected("USD")

        coVerify { userPreferences.setCurrencyCode("USD") }
        assertFalse(viewModel.uiState.value.isCurrencyPickerVisible)
    }

    @Test
    fun `setThemeMode delegates to UserPreferences`() = runTest {
        viewModel.setThemeMode(Constants.ThemeMode.DARK)

        coVerify { userPreferences.setThemeMode(Constants.ThemeMode.DARK) }
    }

    @Test
    fun `a failure while collecting preferences becomes a UiError instead of crashing`() {
        val brokenPreferences = mockk<UserPreferences>(relaxed = true) {
            every { currencyCode } returns flow { throw RuntimeException("DataStore unavailable") }
            every { themeMode } returns themeModeFlow
        }

        val brokenViewModel = SettingsViewModel(brokenPreferences, context)

        assertNotNull(brokenViewModel.uiState.value.error)
    }

    @Test
    fun `clearError resets the error back to null`() {
        val brokenPreferences = mockk<UserPreferences>(relaxed = true) {
            every { currencyCode } returns flow { throw RuntimeException("boom") }
            every { themeMode } returns themeModeFlow
        }
        val brokenViewModel = SettingsViewModel(brokenPreferences, context)

        brokenViewModel.clearError()

        assertNull(brokenViewModel.uiState.value.error)
    }
}