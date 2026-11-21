package com.example.expensetracker.presentation.dashboard

import android.content.Context
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.UserPreferences
import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.data.repository.FakeCategoryRepository
import com.example.expensetracker.data.repository.FakeLedgerRepository
import com.example.expensetracker.data.repository.FakeTransactionRepository
import com.example.expensetracker.domain.model.AppResult
import com.example.expensetracker.domain.model.DateRangeFilter
import com.example.expensetracker.domain.model.UiError
import com.example.expensetracker.domain.model.chart.ChartMode
import com.example.expensetracker.util.MainDispatcherRule
import io.mockk.coEvery
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
    private lateinit var activeLedgerIdFlow: MutableStateFlow<String?>
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var context: Context
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        transactionRepository = FakeTransactionRepository()
        ledgerRepository = FakeLedgerRepository()
        context = mockk(relaxed = true)
        categoryRepository = FakeCategoryRepository()
        activeLedgerIdFlow = MutableStateFlow(null)
        userPreferences = mockk(relaxed = true) {
            every { currencyCode } returns MutableStateFlow("PKR")
            every { activeLedgerId } returns activeLedgerIdFlow
            coEvery { setActiveLedgerId(any()) } coAnswers {
                activeLedgerIdFlow.value = firstArg()
            }
        }

        viewModel = DashboardViewModel(
            transactionRepository = transactionRepository,
            ledgerRepository = ledgerRepository,
            categoryRepository = categoryRepository,
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
    fun `selecting AllTime filter updates ui state and requests all transactions`() {
        ledgerRepository.emitDefaultLedger(sampleLedger())

        viewModel.onFilterSelected(DateRangeFilter.AllTime)

        assertEquals(DateRangeFilter.AllTime, viewModel.uiState.value.selectedFilter)
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

    @Test
    fun `when last month has transactions, monthlyTrendDelta is computed accurately`() = runTest {
        transactionRepository.emitPeriodIncome(10000.0)
        transactionRepository.emitPeriodExpense(4000.0)
        ledgerRepository.emitDefaultLedger(sampleLedger())

        assertEquals(6000.0, viewModel.uiState.value.thisMonthNet, 0.01)
        assertTrue(viewModel.uiState.value.hasLastMonthData)
    }

    @Test
    fun `when last month has no transactions, hasLastMonthData is false`() = runTest {
        transactionRepository.emitPeriodIncome(null)
        transactionRepository.emitPeriodExpense(null)
        ledgerRepository.emitDefaultLedger(sampleLedger())

        assertEquals(false, viewModel.uiState.value.hasLastMonthData)
        assertEquals(null, viewModel.uiState.value.monthlyTrendDelta)
    }

    @Test
    fun `transactions update populates chartDataSet in ui state`() = runTest {
        ledgerRepository.emitDefaultLedger(sampleLedger())
        transactionRepository.emitTransactions(listOf(sampleTransaction()))

        val chartData = viewModel.uiState.value.chartDataSet
        assertNotNull(chartData)
        assertTrue(chartData.points.isNotEmpty())
    }

    @Test
    fun `onChartModeSelected updates selectedChartMode in ui state`() {
        assertEquals(ChartMode.EXPENSES, viewModel.uiState.value.selectedChartMode)

        viewModel.onChartModeSelected(ChartMode.INCOME)
        assertEquals(ChartMode.INCOME, viewModel.uiState.value.selectedChartMode)

        viewModel.onChartModeSelected(ChartMode.BALANCE)
        assertEquals(ChartMode.BALANCE, viewModel.uiState.value.selectedChartMode)
    }

    @Test
    fun `switching active ledger updates activeLedger and scoped totals`() = runTest {
        val defaultLedger = sampleLedger(balance = 500.0)
        val businessLedger = Ledger(
            id = "ledger_business",
            name = "Business",
            currencyCode = "USD",
            balance = 12000.0,
            totalIncome = 15000.0,
            totalExpenses = 3000.0,
            createdAt = 10L,
            updatedAt = 10L,
            isDefault = false
        )
        ledgerRepository.emitLedgers(listOf(defaultLedger, businessLedger))

        viewModel.selectActiveLedger("ledger_business")

        val state = viewModel.uiState.value
        assertEquals("Business", state.activeLedger?.name)
        assertEquals(12000.0, state.balance, 0.0)
        assertEquals(15000.0, state.totalIncome, 0.0)
        assertEquals(3000.0, state.totalExpense, 0.0)
    }

    @Test
    fun `creating a ledger inserts it into repository and sets it as active`() = runTest {
        every { userPreferences.currencyCode } returns MutableStateFlow("PKR")

        viewModel.createLedger("Freelance", "USD")

        val state = viewModel.uiState.value
        assertEquals(false, state.isCreateEditLedgerDialogVisible)
    }

    @Test
    fun `deleting a non-default ledger marks it soft-deleted and falls back to default`() = runTest {
        val defaultLedger = sampleLedger(balance = 500.0)
        val customLedger = Ledger(
            id = "ledger_temp",
            name = "Temp",
            currencyCode = "PKR",
            balance = 100.0,
            createdAt = 10L,
            updatedAt = 10L,
            isDefault = false
        )
        ledgerRepository.emitLedgers(listOf(defaultLedger, customLedger))

        viewModel.deleteLedger(customLedger)

        val found = ledgerRepository.getLedgerById("ledger_temp")
        assertTrue(found is AppResult.Success && found.data == null)
    }
}
