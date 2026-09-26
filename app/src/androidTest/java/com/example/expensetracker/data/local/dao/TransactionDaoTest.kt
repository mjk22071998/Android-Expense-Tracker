package com.example.expensetracker.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.database.AppDatabase
import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.data.local.entity.Transaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var transactionDao: TransactionDao
    private lateinit var ledgerDao: LedgerDao
    private lateinit var snapshotDao: MonthlySnapshotDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        transactionDao = db.transactionDao()
        ledgerDao = db.ledgerDao()
        snapshotDao = db.monthlySnapshotDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    private suspend fun insertSampleLedger(id: String = "ledger_1"): Ledger {
        val ledger = Ledger(
            id = id,
            name = "Personal",
            currencyCode = "PKR",
            balance = 0.0,
            totalIncome = 0.0,
            totalExpenses = 0.0,
            createdAt = 0L,
            updatedAt = 0L,
            isDefault = true
        )
        ledgerDao.insert(ledger)
        return ledger
    }

    private fun sampleTransaction(
        id: String = "txn_1",
        ledgerId: String = "ledger_1",
        amount: Double = 100.0,
        type: String = Constants.TransactionType.EXPENSE,
        date: Long = 0L
    ) = Transaction(
        id = id,
        ledgerId = ledgerId,
        categoryId = null,
        amount = amount,
        type = type,
        note = "Test",
        date = date,
        createdAt = 0L,
        updatedAt = 0L,
        isDeleted = false
    )

    @Test
    fun insertingAnExpenseReducesLedgerBalance() = runTest {
        insertSampleLedger()

        transactionDao.insertAndUpdateBalance(
            sampleTransaction(amount = 100.0, type = Constants.TransactionType.EXPENSE),
            ledgerDao,
            snapshotDao
        )

        val ledger = ledgerDao.getLedgerById("ledger_1")
        assertEquals(-100.0, ledger?.balance ?: 0.0, 0.0)
        assertEquals(100.0, ledger?.totalExpenses ?: 0.0, 0.0)
    }

    @Test
    fun insertingIncomeIncreasesLedgerBalance() = runTest {
        insertSampleLedger()

        transactionDao.insertAndUpdateBalance(
            sampleTransaction(amount = 200.0, type = Constants.TransactionType.INCOME),
            ledgerDao,
            snapshotDao
        )

        val ledger = ledgerDao.getLedgerById("ledger_1")
        assertEquals(200.0, ledger?.balance ?: 0.0, 0.0)
        assertEquals(200.0, ledger?.totalIncome ?: 0.0, 0.0)
    }

    @Test
    fun editingATransactionFromExpenseToIncomeCorrectlyReversesAndReapplies() = runTest {
        insertSampleLedger()
        val original = sampleTransaction(amount = 100.0, type = Constants.TransactionType.EXPENSE)
        transactionDao.insertAndUpdateBalance(original, ledgerDao, snapshotDao)

        // ledger should be at -100.0 right now — confirm before mutating further
        val afterInsert = ledgerDao.getLedgerById("ledger_1")
        assertEquals(-100.0, afterInsert?.balance ?: 0.0, 0.0)

        val edited = original.copy(amount = 100.0, type = Constants.TransactionType.INCOME)
        transactionDao.updateAndRecalculateBalance(original, edited, ledgerDao, snapshotDao)

        val ledger = ledgerDao.getLedgerById("ledger_1")
        // -100 (expense) reversed back to 0, then +100 (income) applied = +100
        assertEquals(100.0, ledger?.balance ?: 0.0, 0.0)
        assertEquals(0.0, ledger?.totalExpenses ?: 0.0, 0.0)
        assertEquals(100.0, ledger?.totalIncome ?: 0.0, 0.0)
    }

    @Test
    fun softDeletingATransactionReversesItsEffectOnBalance() = runTest {
        insertSampleLedger()
        val transaction = sampleTransaction(amount = 150.0, type = Constants.TransactionType.EXPENSE)
        transactionDao.insertAndUpdateBalance(transaction, ledgerDao, snapshotDao)

        transactionDao.softDeleteAndUpdateBalance(transaction, ledgerDao, snapshotDao)

        val ledger = ledgerDao.getLedgerById("ledger_1")
        assertEquals(0.0, ledger?.balance ?: 0.0, 0.0)
        assertEquals(0.0, ledger?.totalExpenses ?: 0.0, 0.0)
    }

    @Test
    fun softDeletedTransactionsDoNotAppearInGetTransactions() = runTest {
        insertSampleLedger()
        val transaction = sampleTransaction()
        transactionDao.insertAndUpdateBalance(transaction, ledgerDao, snapshotDao)
        transactionDao.softDeleteAndUpdateBalance(transaction, ledgerDao, snapshotDao)

        val results = transactionDao.getTransactions(
            ledgerId = "ledger_1",
            startDate = -1000L,
            endDate = 1000L
        ).first()

        assertTrue(results.isEmpty())
    }
}