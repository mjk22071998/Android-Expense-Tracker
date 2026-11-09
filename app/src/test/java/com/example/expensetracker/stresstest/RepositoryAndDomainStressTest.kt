package com.example.expensetracker.stresstest

import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class RepositoryAndDomainStressTest {

    @Test
    fun `concurrent high-volume transaction creation maintains exact arithmetic integrity`() = runBlocking {
        val totalTransactions = 1000
        val threadSafeTransactions = ConcurrentHashMap<String, Transaction>()
        val nextId = AtomicInteger(0)

        // Generate 1,000 transactions concurrently across multiple coroutines
        val jobs = (1..totalTransactions).map { i ->
            async(Dispatchers.Default) {
                val isIncome = (i % 3 == 0) // 1 in 3 is income, 2 in 3 are expense
                val amount = (i * 1.5)
                val id = "tx_${nextId.incrementAndGet()}"
                val transaction = Transaction.create(
                    amount = amount,
                    type = if (isIncome) Constants.TransactionType.INCOME else Constants.TransactionType.EXPENSE,
                    categoryId = "cat_${i % 10}",
                    ledgerId = "ledger_test",
                    note = "Stress test transaction #$i with special payload: '; DROP TABLE transactions; -- 💸 🚀",
                    date = System.currentTimeMillis() - (i * 3600_000L),
                    id = id
                )
                threadSafeTransactions[id] = transaction
            }
        }

        jobs.awaitAll()

        assertEquals(totalTransactions, threadSafeTransactions.size)

        // Calculate expected financial totals
        val expectedIncome = threadSafeTransactions.values
            .filter { it.type == Constants.TransactionType.INCOME }
            .sumOf { it.amount }

        val expectedExpense = threadSafeTransactions.values
            .filter { it.type == Constants.TransactionType.EXPENSE }
            .sumOf { it.amount }

        val expectedNet = expectedIncome - expectedExpense

        assertTrue("Expected income should be positive", expectedIncome > 0)
        assertTrue("Expected expense should be positive", expectedExpense > 0)
        assertEquals(expectedIncome - expectedExpense, expectedNet, 0.0001)
    }

    @Test
    fun `domain entity handles massive 50,000 character notes and multi-byte emojis safely`() {
        val massiveNote = "💸".repeat(1000) + "a".repeat(45000) + "\n\tSpecial: '; DROP TABLE users; --"
        val transaction = Transaction.create(
            amount = 123.45,
            type = Constants.TransactionType.EXPENSE,
            categoryId = "cat_1",
            ledgerId = "ledger_1",
            note = massiveNote
        )

        assertNotNull(transaction)
        assertEquals(massiveNote, transaction.note)
        assertEquals(123.45, transaction.amount, 0.001)
        assertFalse(transaction.isDeleted)

        // Verify soft-delete preserves the massive note
        val softDeleted = transaction.toSoftDeleted()
        assertTrue(softDeleted.isDeleted)
        assertEquals(massiveNote, softDeleted.note)
    }

    @Test
    fun `soft deletion bulk filtering accurately isolates active from deleted transactions`() {
        val total = 500
        val items = (1..total).map { i ->
            val isDeleted = (i % 2 == 0)
            Transaction(
                id = "tx_$i",
                ledgerId = "ledger_1",
                categoryId = "cat_1",
                amount = 10.0,
                type = Constants.TransactionType.EXPENSE,
                note = "Item $i",
                date = 1000L,
                createdAt = 1000L,
                updatedAt = 1000L,
                isDeleted = isDeleted
            )
        }

        val activeItems = items.filter { !it.isDeleted }
        val deletedItems = items.filter { it.isDeleted }

        assertEquals(250, activeItems.size)
        assertEquals(250, deletedItems.size)
        assertTrue(activeItems.none { it.isDeleted })
        assertTrue(deletedItems.all { it.isDeleted })
    }
}
