package com.example.expensetracker.presentation.dashboard

import android.content.Context
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.UserPreferences
import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.data.repository.FakeLedgerRepository
import com.example.expensetracker.data.repository.FakeTransactionRepository
import com.example.expensetracker.domain.model.AppResult
import com.example.expensetracker.domain.model.DateRangeFilter
import com.example.expensetracker.domain.model.UiError
import com.example.expensetracker.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var ledgerRepository: FakeLedgerRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var context: Context
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        transactionRepository = FakeTransactionRepository()
        ledgerRepository = FakeLedgerRepository()
        context = mockk(relaxed = true)
        userPreferences = mockk(relaxed = true) {
            every { currencyCode } returns MutableStateFlow("PKR")
        }

        viewModel = DashboardViewModel(
            transactionRepository = transactionRepository,
            ledgerRepository = ledgerRepository,
            userPreferences = userPreferences,
            context = context
        )
    }

    private fun sampleLedger(
        balance: Double = 500.0,
        totalIncome: Double = 1000.0,
        totalExpenses: Double = 500.0
    ) = Ledger(
        id = "ledger_1",
        name = "Personal",
        currencyCode = "PKR",
        balance = balance,
        totalIncome = totalIncome,
        totalExpenses = totalExpenses,
        createdAt = 0L,
        updatedAt = 0L,
        isDefault = true
    )

    private fun sampleTransaction(
        id: String = "txn_1",
        type: String = Constants.TransactionType.EXPENSE
    ) = TransactionWithCategory(
        id = id,
        ledgerId = "ledger_1",
        categoryId = "cat_food",
        categoryIcon = "restaurant",
        categoryName = "Food",
        amount = 250.0,
        type = type,
        note = "Lunch",
        date = 0L,
        createdAt = 0L,
        updatedAt = 0L,
        isDeleted = false
    )

    @Test
    fun `loading the default ledger populates balance and totals`() {
        ledgerRepository.emitDefaultLedger(sampleLedger())

        val state = viewModel.uiState.value

        assertEquals(500.0, state.balance, 0.0)
        assertEquals(1000.0, state.totalIncome, 0.0)
        assertEquals(500.0, state.totalExpense, 0.0)
    }

    @Test
    fun `once a default ledger exists its transactions are loaded`() {
        ledgerRepository.emitDefaultLedger(sampleLedger())
        transactionRepository.emitTransactions(listOf(sampleTransaction()))

        assertEquals(1, viewModel.uiState.value.transactions.size)
    }

    @Test
    fun `changing the filter updates the selected filter in ui state`() {
        ledgerRepository.emitDefaultLedger(sampleLedger())

        viewModel.onFilterSelected(DateRangeFilter.ThisMonth)

        assertEquals(DateRangeFilter.ThisMonth, viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun `soft deleting a transaction marks it deleted and sends it to the repository`() = runTest {
        ledgerRepository.emitDefaultLedger(sampleLedger())
        val transaction = sampleTransaction()

        viewModel.softDeleteTransaction(transaction)

        assertEquals(transaction.id, transactionRepository.lastSoftDeletedTransaction?.id)
        assertTrue(transactionRepository.lastSoftDeletedTransaction?.isDeleted == true)
    }

    @Test
    fun `a failed soft delete populates the error state instead of throwing`() = runTest {
        ledgerRepository.emitDefaultLedger(sampleLedger())
        transactionRepository.softDeleteResult = AppResult.Error(UiError.Database())

        viewModel.softDeleteTransaction(sampleTransaction())

        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `a failure while collecting transactions becomes a UiError instead of crashing`() {
        ledgerRepository.emitDefaultLedger(sampleLedger())
        transactionRepository.errorToThrow = RuntimeException("Room query failed")

        viewModel.onFilterSelected(DateRangeFilter.ThisWeek) // re-triggers loadTransactions()

        assertNotNull(viewModel.uiState.value.error)
    }
}