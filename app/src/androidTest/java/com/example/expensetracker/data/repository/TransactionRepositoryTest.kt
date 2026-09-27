package com.example.expensetracker.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.database.AppDatabase
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.domain.model.AppResult
import com.example.expensetracker.domain.model.UiError
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: TransactionRepository

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

        // Deliberately real DAOs, not Fakes — the entire point of this test is
        // proving safeCall's behavior against a genuine Room/SQLite exception,
        // which a Fake could never produce in the first place.
        repository = TransactionRepositoryImpl(
            transactionDao = db.transactionDao(),
            ledgerDao = db.ledgerDao(),
            snapshotDao = db.monthlySnapshotDao()
        )
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertingATransactionForALedgerThatDoesNotExistReturnsADatabaseError() = runTest {
        // No ledger is ever inserted here — "ledger_missing" has no matching row,
        // which should trip the real foreign key constraint on transactions.ledgerId.
        val orphanTransaction = Transaction(
            id = "txn_orphan",
            ledgerId = "ledger_missing",
            categoryId = null,
            amount = 100.0,
            type = Constants.TransactionType.EXPENSE,
            note = "Should fail",
            date = 0L,
            createdAt = 0L,
            updatedAt = 0L,
            isDeleted = false
        )

        val result = repository.insertTransaction(orphanTransaction)

        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).error is UiError.Database)
    }
}