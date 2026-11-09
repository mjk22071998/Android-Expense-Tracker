package com.example.expensetracker.presentation.transaction

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.UserPreferences
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.data.repository.FakeCategoryRepository
import com.example.expensetracker.data.repository.FakeLedgerRepository
import com.example.expensetracker.data.repository.FakeTransactionRepository
import com.example.expensetracker.domain.model.AppResult
import com.example.expensetracker.domain.model.UiError
import com.example.expensetracker.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEditTransactionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val currencyCodeFlow = MutableStateFlow("USD")
    private val themeModeFlow = MutableStateFlow(Constants.ThemeMode.SYSTEM)

    private lateinit var userPreferences: UserPreferences
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var ledgerRepository: FakeLedgerRepository
    private lateinit var context: Context

    private val defaultLedger = Ledger(
        id = "ledger_1",
        name = "Primary",
        currencyCode = "USD",
        balance = 500.0,
        totalIncome = 1000.0,
        totalExpenses = 500.0,
        isDefault = true,
        createdAt = 1000L,
        updatedAt = 1000L
    )

    private val sampleCategories = listOf(
        Category(
            id = "cat_expense_1",
            name = "Food",
            icon = "ic_food",
            transactionType = Constants.TransactionType.EXPENSE,
            isDefault = true
        ),
        Category(
            id = "cat_income_1",
            name = "Salary",
            icon = "ic_salary",
            transactionType = Constants.TransactionType.INCOME,
            isDefault = true
        )
    )

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

    private fun createViewModel(transactionId: String? = null): AddEditTransactionViewModel {
        val savedState = SavedStateHandle().apply {
            if (transactionId != null) {
                set(Constants.NavArgs.TRANSACTION_ID, transactionId)
            }
        }
        return AddEditTransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            ledgerRepository = ledgerRepository,
            userPreferences = userPreferences,
            savedStateHandle = savedState,
            context = context
        )
    }

    @Test
    fun `initial state in add mode has isEditMode false and isSaveEnabled false`() {
        val viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.isEditMode)
        assertFalse(viewModel.uiState.value.isSaveEnabled)
    }

    @Test
    fun `entering valid amount and category enables save button only when ledger is loaded`() {
        val viewModel = createViewModel()

        viewModel.onAmountChanged("45.50")
        viewModel.onCategorySelected("cat_expense_1")

        // Ledger has not loaded yet, so save must remain disabled
        assertFalse(viewModel.uiState.value.isSaveEnabled)

        // Now ledger loads
        ledgerRepository.emitDefaultLedger(defaultLedger)

        assertTrue(viewModel.uiState.value.isSaveEnabled)
    }

    @Test
    fun `switching transaction type filters category list accordingly`() {
        val viewModel = createViewModel()
        categoryRepository.emitCategories(sampleCategories)

        // Default type is EXPENSE
        assertEquals(1, viewModel.uiState.value.filteredCategories.size)
        assertEquals("cat_expense_1", viewModel.uiState.value.filteredCategories.first().id)

        // Switch to INCOME
        viewModel.onTypeChanged(Constants.TransactionType.INCOME)
        assertEquals(1, viewModel.uiState.value.filteredCategories.size)
        assertEquals("cat_income_1", viewModel.uiState.value.filteredCategories.first().id)
    }

    @Test
    fun `saveTransaction inserts new record and marks state as saved`() = runTest {
        val viewModel = createViewModel()
        ledgerRepository.emitDefaultLedger(defaultLedger)
        categoryRepository.emitCategories(sampleCategories)

        viewModel.onAmountChanged("75.00")
        viewModel.onCategorySelected("cat_expense_1")
        viewModel.onNoteChanged("Grocery shopping")

        viewModel.saveTransaction()

        assertTrue(viewModel.uiState.value.isSaved)
        val inserted = transactionRepository.insertedTransaction
        assertNotNull(inserted)
        assertEquals(75.00, inserted!!.amount, 0.001)
        assertEquals("cat_expense_1", inserted.categoryId)
        assertEquals("ledger_1", inserted.ledgerId)
        assertEquals("Grocery shopping", inserted.note)
    }

    @Test
    fun `edit mode loads existing transaction and saving updates it`() = runTest {
        val existing = Transaction(
            id = "tx_99",
            ledgerId = "ledger_1",
            categoryId = "cat_income_1",
            amount = 1500.0,
            type = Constants.TransactionType.INCOME,
            note = "Original bonus",
            date = 1000L,
            createdAt = 1000L,
            updatedAt = 1000L,
            isDeleted = false
        )
        transactionRepository.transactionToReturn = existing
        ledgerRepository.emitDefaultLedger(defaultLedger)
        categoryRepository.emitCategories(sampleCategories)

        val viewModel = createViewModel(transactionId = "tx_99")

        assertTrue(viewModel.uiState.value.isEditMode)
        assertEquals("1500.0", viewModel.uiState.value.amount)
        assertEquals(Constants.TransactionType.INCOME, viewModel.uiState.value.type)
        assertEquals("cat_income_1", viewModel.uiState.value.selectedCategoryId)

        viewModel.onAmountChanged("2000.0")
        viewModel.saveTransaction()

        assertTrue(viewModel.uiState.value.isSaved)
        val updated = transactionRepository.updatedTransaction
        assertNotNull(updated)
        assertEquals("tx_99", updated!!.id)
        assertEquals(2000.0, updated.amount, 0.001)
    }

    @Test
    fun `when repository returns error on save, uiState error is populated`() = runTest {
        val viewModel = createViewModel()
        ledgerRepository.emitDefaultLedger(defaultLedger)
        categoryRepository.emitCategories(sampleCategories)

        val dbError = UiError.Database("Failed to insert record")
        transactionRepository.insertTransactionResult = AppResult.Error(dbError)

        viewModel.onAmountChanged("10.0")
        viewModel.onCategorySelected("cat_expense_1")
        viewModel.saveTransaction()

        assertFalse(viewModel.uiState.value.isSaved)
        assertEquals(dbError, viewModel.uiState.value.error)
    }
}
