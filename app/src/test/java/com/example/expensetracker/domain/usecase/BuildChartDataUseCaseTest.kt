package com.example.expensetracker.domain.usecase

import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.domain.model.DateRangeFilter
import com.example.expensetracker.domain.model.chart.ChartPeriodType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class BuildChartDataUseCaseTest {

    private lateinit var useCase: BuildChartDataUseCase

    @Before
    fun setUp() {
        useCase = BuildChartDataUseCase()
    }

    private fun createTransaction(
        id: String,
        amount: Double,
        type: String,
        date: Long
    ): TransactionWithCategory {
        return TransactionWithCategory(
            id = id,
            ledgerId = "ledger_default",
            categoryId = "cat_general",
            categoryIcon = "category",
            categoryName = "General",
            amount = amount,
            type = type,
            note = "Test Note",
            date = date,
            createdAt = date,
            updatedAt = date,
            isDeleted = false
        )
    }

    @Test
    fun `ThisWeek produces exactly 7 daily bins Monday through Sunday`() {
        // Fixed reference Wednesday: 2026-10-14 12:00:00
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 14, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val refNow = cal.timeInMillis

        val result = useCase.execute(
            transactions = emptyList(),
            filter = DateRangeFilter.ThisWeek,
            initialBalance = 1000.0,
            now = refNow
        )

        assertEquals(7, result.points.size)
        assertEquals(ChartPeriodType.DAILY, result.periodType)
        assertEquals("Mon", result.points[0].label)
        assertEquals("Sun", result.points[6].label)
    }

    @Test
    fun `Zero-activity days show 0 for expense and income, but carry forward running balance`() {
        // Wednesday Oct 14, 2026
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 14, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val refNow = cal.timeInMillis

        // Monday Oct 12: Expense 200.0
        val monCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 12, 10, 0, 0)
        }
        val monTx = createTransaction("tx1", 200.0, Constants.TransactionType.EXPENSE, monCal.timeInMillis)

        // Wednesday Oct 14: Income 500.0
        val wedCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 14, 15, 0, 0)
        }
        val wedTx = createTransaction("tx2", 500.0, Constants.TransactionType.INCOME, wedCal.timeInMillis)

        val result = useCase.execute(
            transactions = listOf(monTx, wedTx),
            filter = DateRangeFilter.ThisWeek,
            initialBalance = 1000.0,
            now = refNow
        )

        val mon = result.points[0] // Monday
        val tue = result.points[1] // Tuesday (Zero activity!)
        val wed = result.points[2] // Wednesday
        val thu = result.points[3] // Thursday (Zero activity!)

        // Monday
        assertEquals(200.0, mon.expense, 0.001)
        assertEquals(0.0, mon.income, 0.001)
        assertEquals(800.0, mon.runningBalance, 0.001) // 1000 - 200

        // Tuesday (No transactions: flat carryforward!)
        assertEquals(0.0, tue.expense, 0.001)
        assertEquals(0.0, tue.income, 0.001)
        assertEquals(800.0, tue.runningBalance, 0.001) // carried forward from Monday!
        assertEquals(0, tue.transactionCount)

        // Wednesday
        assertEquals(0.0, wed.expense, 0.001)
        assertEquals(500.0, wed.income, 0.001)
        assertEquals(1300.0, wed.runningBalance, 0.001) // 800 + 500

        // Thursday (No transactions: flat carryforward from Wednesday!)
        assertEquals(0.0, thu.expense, 0.001)
        assertEquals(0.0, thu.income, 0.001)
        assertEquals(1300.0, thu.runningBalance, 0.001) // carried forward from Wednesday!
        assertEquals(0, thu.transactionCount)
    }

    @Test
    fun `ThisMonth produces daily bins matching the total days in the active month`() {
        // October has 31 days
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 5, 12, 0, 0)
        }
        val refNow = cal.timeInMillis

        val result = useCase.execute(
            transactions = emptyList(),
            filter = DateRangeFilter.ThisMonth,
            initialBalance = 500.0,
            now = refNow
        )

        assertEquals(31, result.points.size)
        assertEquals(ChartPeriodType.DAILY, result.periodType)
        assertEquals("1", result.points[0].label)
        assertEquals("31", result.points[30].label)
        // All points carry forward initial balance
        result.points.forEach { point ->
            assertEquals(500.0, point.runningBalance, 0.001)
            assertEquals(0.0, point.expense, 0.001)
            assertEquals(0.0, point.income, 0.001)
        }
    }

    @Test
    fun `ThisYear produces 12 monthly bins from Jan to Dec`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.MAY, 15, 12, 0, 0)
        }
        val refNow = cal.timeInMillis

        // March expense: 1500
        val marCal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 10, 10, 0, 0)
        }
        val marTx = createTransaction("tx_mar", 1500.0, Constants.TransactionType.EXPENSE, marCal.timeInMillis)

        val result = useCase.execute(
            transactions = listOf(marTx),
            filter = DateRangeFilter.ThisYear,
            initialBalance = 2000.0,
            now = refNow
        )

        assertEquals(12, result.points.size)
        assertEquals(ChartPeriodType.MONTHLY, result.periodType)
        assertEquals("Jan", result.points[0].label)
        assertEquals("Dec", result.points[11].label)

        // Jan and Feb: balance 2000
        assertEquals(2000.0, result.points[0].runningBalance, 0.001)
        assertEquals(2000.0, result.points[1].runningBalance, 0.001)

        // Mar: expense 1500, balance 500
        assertEquals(1500.0, result.points[2].expense, 0.001)
        assertEquals(500.0, result.points[2].runningBalance, 0.001)

        // Apr through Dec: carry forward 500
        for (i in 3..11) {
            assertEquals(500.0, result.points[i].runningBalance, 0.001)
            assertEquals(0.0, result.points[i].expense, 0.001)
        }
    }

    @Test
    fun `Multiple transactions on the same day are aggregated correctly`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 14, 12, 0, 0)
        }
        val refNow = cal.timeInMillis

        val tx1 = createTransaction("tx1", 100.0, Constants.TransactionType.EXPENSE, refNow)
        val tx2 = createTransaction("tx2", 250.0, Constants.TransactionType.EXPENSE, refNow)
        val tx3 = createTransaction("tx3", 1000.0, Constants.TransactionType.INCOME, refNow)

        val result = useCase.execute(
            transactions = listOf(tx1, tx2, tx3),
            filter = DateRangeFilter.ThisWeek,
            initialBalance = 0.0,
            now = refNow
        )

        val wed = result.points[2] // Wednesday Oct 14
        assertEquals(350.0, wed.expense, 0.001)
        assertEquals(1000.0, wed.income, 0.001)
        assertEquals(650.0, wed.net, 0.001)
        assertEquals(3, wed.transactionCount)
        assertEquals(650.0, wed.runningBalance, 0.001)
    }

    @Test
    fun `Peak scales and totals are calculated accurately`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 14, 12, 0, 0)
        }
        val refNow = cal.timeInMillis

        val tx1 = createTransaction("tx1", 500.0, Constants.TransactionType.EXPENSE, refNow)
        val tx2 = createTransaction("tx2", 1200.0, Constants.TransactionType.INCOME, refNow)

        val result = useCase.execute(
            transactions = listOf(tx1, tx2),
            filter = DateRangeFilter.ThisWeek,
            initialBalance = 100.0,
            now = refNow
        )

        assertEquals(500.0, result.totalExpense, 0.001)
        assertEquals(1200.0, result.totalIncome, 0.001)
        assertEquals(500.0, result.maxExpense, 0.001)
        assertEquals(1200.0, result.maxIncome, 0.001)
        assertEquals(800.0, result.maxBalance, 0.001) // 100 - 500 + 1200 = 800
        assertFalse(result.isEmpty)
    }
}
