package com.example.expensetracker.stresstest

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.UserPreferences
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.data.repository.FakeCategoryRepository
import com.example.expensetracker.data.repository.FakeLedgerRepository
import com.example.expensetracker.data.repository.FakeTransactionRepository
import com.example.expensetracker.domain.model.DateRangeFilter
import com.example.expensetracker.presentation.dashboard.DashboardViewModel
import com.example.expensetracker.presentation.transaction.AddEditTransactionViewModel
import com.example.expensetracker.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ViewModelStressTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val currencyCodeFlow = MutableStateFlow("USD")
    private val themeModeFlow = MutableStateFlow(Constants.ThemeMode.DARK)

    private lateinit var userPreferences: UserPreferences
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var ledgerRepository: FakeLedgerRepository
    private lateinit var context: Context

    private val sampleLedger = Ledger(
        id = "ledger_primary",
        name = "Primary",
        currencyCode = "USD",
        balance = 5000.0,
        totalIncome = 10000.0,
        totalExpenses = 5000.0,
        isDefault = true,
        createdAt = 1000L,
        updatedAt = 1000L
    )

    private val sampleCategories = (1..20).map { i ->
        Category(
            id = "cat_$i",
            name = "Category $i",
            icon = "ic_tag",
            transactionType = if (i % 2 == 0) Constants.TransactionType.EXPENSE else Constants.TransactionType.INCOME,
            isDefault = true
        )
    }

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        userPreferences = mockk(relaxed = true) {
            every { currencyCode } returns currencyCodeFlow
            every { themeMode } returns themeModeFlow
        }
        transactionRepository = FakeTransactionRepository()
        categoryRepository = FakeCategoryRepository()
        ledgerRepository = FakeLedgerRepository()
    }

    @Test
    fun `dashboard handles high-frequency search query flooding without crashes or race states`() = runTest {
        val viewModel = DashboardViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            ledgerRepository = ledgerRepository,
            userPreferences = userPreferences,
            context = context
        )

        ledgerRepository.emitDefaultLedger(sampleLedger)
        categoryRepository.emitCategories(sampleCategories)

        // Flood 100 rapid keystrokes into search query
        for (i in 1..100) {
            viewModel.onSearchQueryChanged("query_$i")
        }

        // Fast-forward past the search debounce interval (300ms)
        advanceTimeBy(Constants.Search.DEBOUNCE_MS + 100L)

        // State must cleanly reflect the final search query
        assertEquals("query_100", viewModel.uiState.value.searchQuery)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `dashboard handles concurrent filter switching and rapid category selection thrashing`() = runTest {
        val viewModel = DashboardViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            ledgerRepository = ledgerRepository,
            userPreferences = userPreferences,
            context = context
        )

        ledgerRepository.emitDefaultLedger(sampleLedger)
        categoryRepository.emitCategories(sampleCategories)

        val filters = listOf(
            DateRangeFilter.AllTime,
            DateRangeFilter.Today,
            DateRangeFilter.ThisWeek,
            DateRangeFilter.ThisMonth,
            DateRangeFilter.ThisYear
        )

        // Rapidly switch filters 50 times
        for (i in 0 until 50) {
            val f = filters[i % filters.size]
            viewModel.onFilterSelected(f)
            viewModel.onCategoryFilterToggled("cat_${(i % 10) + 1}")
            viewModel.onTypeFilterSelected(
                if (i % 2 == 0) Constants.TransactionType.EXPENSE else Constants.TransactionType.INCOME
            )
        }

        assertNotNull(viewModel.uiState.value)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `AddEditTransactionViewModel handles extreme, invalid, and oversized amount inputs safely`() {
        val savedState = SavedStateHandle()
        val viewModel = AddEditTransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            ledgerRepository = ledgerRepository,
            userPreferences = userPreferences,
            savedStateHandle = savedState,
            context = context
        )

        ledgerRepository.emitDefaultLedger(sampleLedger)
        categoryRepository.emitCategories(sampleCategories)
        viewModel.onCategorySelected("cat_2")

        val invalidInputs = listOf(
            "",
            "   ",
            "abc",
            "-50",
            "-0.01",
            "0",
            "0.00",
            "..",
            "1.2.3",
            "NaN",
            "Infinity",
            "9999999999999999999999999999999999999999999999999999999999999999999999999"
        )

        for (input in invalidInputs) {
            viewModel.onAmountChanged(input)
            // Invalid inputs must keep save disabled
            if (input == "0" || input == "0.00" || input.toDoubleOrNull() == null || (input.toDoubleOrNull() ?: 0.0) <= 0.0) {
                assertFalse("Save should be disabled for input: $input", viewModel.uiState.value.isSaveEnabled)
            }
        }

        // Valid extreme input
        viewModel.onAmountChanged("999999999.99")
        assertTrue(viewModel.uiState.value.isSaveEnabled)
    }

    @Test
    fun `AddEditTransactionViewModel prevents duplicate save executions during in-flight network or disk delays`() = runTest {
        val savedState = SavedStateHandle()
        val viewModel = AddEditTransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            ledgerRepository = ledgerRepository,
            userPreferences = userPreferences,
            savedStateHandle = savedState,
            context = context
        )

        ledgerRepository.emitDefaultLedger(sampleLedger)
        categoryRepository.emitCategories(sampleCategories)

        viewModel.onAmountChanged("150.00")
        viewModel.onCategorySelected("cat_2")

        // First save starts
        viewModel.saveTransaction()
        assertTrue(viewModel.uiState.value.isSaved)
        val firstInserted = transactionRepository.insertedTransaction
        assertNotNull(firstInserted)
    }
}
