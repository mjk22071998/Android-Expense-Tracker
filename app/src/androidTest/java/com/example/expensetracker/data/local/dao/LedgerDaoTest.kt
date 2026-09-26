package com.example.expensetracker.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.expensetracker.data.local.database.AppDatabase
import com.example.expensetracker.data.local.entity.Ledger
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LedgerDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var ledgerDao: LedgerDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        ledgerDao = db.ledgerDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    private fun sampleLedger(
        id: String = "ledger_1",
        balance: Double = 0.0,
        totalIncome: Double = 0.0,
        totalExpenses: Double = 0.0,
        isDefault: Boolean = true
    ) = Ledger(
        id = id,
        name = "Personal",
        currencyCode = "PKR",
        balance = balance,
        totalIncome = totalIncome,
        totalExpenses = totalExpenses,
        createdAt = 0L,
        updatedAt = 0L,
        isDefault = isDefault
    )

    @Test
    fun insertAndReadBackAreConsistent() = runTest {
        ledgerDao.insert(sampleLedger())

        val result = ledgerDao.getLedgerById("ledger_1")

        assertNotNull(result)
        assertEquals("Personal", result?.name)
    }

    @Test
    fun addIncomeIncreasesBothBalanceAndTotalIncome() = runTest {
        ledgerDao.insert(sampleLedger())

        ledgerDao.addIncome(ledgerId = "ledger_1", amount = 100.0, updatedAt = 1L)

        val result = ledgerDao.getLedgerById("ledger_1")
        assertEquals(100.0, result?.balance ?: 0.0, 0.0)
        assertEquals(100.0, result?.totalIncome ?: 0.0, 0.0)
    }

    @Test
    fun deletingADefaultLedgerHasNoEffect() = runTest {
        ledgerDao.insert(sampleLedger(isDefault = true))

        ledgerDao.delete("ledger_1")

        val result = ledgerDao.getLedgerById("ledger_1")
        assertNotNull(result) // still there — WHERE isDefault = 0 protected it
    }
}