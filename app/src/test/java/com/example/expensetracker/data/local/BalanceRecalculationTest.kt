package com.example.expensetracker.data.local

import com.example.expensetracker.data.local.dao.LedgerDao
import com.example.expensetracker.data.local.dao.MonthlySnapshotDao
import com.example.expensetracker.data.local.dao.TransactionDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BalanceRecalculationTest {

    @Test
    fun recalculateDerivedState_computesExactSumsAndUpdateLedgerTotals() = runTest {
        val ledgerDao = mockk<LedgerDao>(relaxed = true)
        val snapshotDao = mockk<MonthlySnapshotDao>(relaxed = true)
        val transactionDao = mockk<TransactionDao>()

        coEvery { transactionDao.getTotalIncome("ledger_01") } returns 500.0
        coEvery { transactionDao.getTotalExpense("ledger_01") } returns 150.0
        coEvery { transactionDao.recalculateDerivedState("ledger_01", ledgerDao, snapshotDao) } answers { callOriginal() }

        transactionDao.recalculateDerivedState("ledger_01", ledgerDao, snapshotDao)

        coVerify {
            ledgerDao.updateTotals(
                ledgerId = "ledger_01",
                balance = 350.0,
                totalIncome = 500.0,
                totalExpenses = 150.0,
                updatedAt = any()
            )
        }
    }
}
