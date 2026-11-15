package com.example.expensetracker.data.remote

import com.example.expensetracker.data.local.SyncStatus
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.data.remote.dto.CategoryDto
import com.example.expensetracker.data.remote.dto.LedgerDto
import com.example.expensetracker.data.remote.dto.TransactionDto
import com.example.expensetracker.data.remote.mapper.toDto
import com.example.expensetracker.data.remote.mapper.toEntity
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyncMappersTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val testUserId = "00000000-0000-0000-0000-000000000001"

    @Test
    fun transaction_toDto_and_toEntity_roundtrip() {
        val entity = Transaction(
            id = "tx-101",
            ledgerId = "ledger-01",
            categoryId = null,
            amount = 45.50,
            type = "EXPENSE",
            note = "Coffee",
            date = 1778306000000L,
            createdAt = 1778306100000L,
            updatedAt = 1778306200000L,
            isDeleted = false,
            syncStatus = SyncStatus.PENDING_UPLOAD,
            serverId = null
        )

        val dto = entity.toDto(testUserId)

        assertEquals("tx-101", dto.id)
        assertEquals(testUserId, dto.userId)
        assertEquals("ledger-01", dto.ledgerId)
        assertNull(dto.categoryId)
        assertEquals(45.50, dto.amount, 0.001)
        assertEquals("EXPENSE", dto.type)
        assertEquals("Coffee", dto.note)
        assertEquals(1778306000000L, dto.date)

        val decodedEntity = dto.toEntity()
        assertEquals(entity.id, decodedEntity.id)
        assertEquals(entity.ledgerId, decodedEntity.ledgerId)
        assertEquals(entity.categoryId, decodedEntity.categoryId)
        assertEquals(entity.amount, decodedEntity.amount, 0.001)
        assertEquals(entity.type, decodedEntity.type)
        assertEquals(entity.note, decodedEntity.note)
        assertEquals(entity.date, decodedEntity.date)
        assertEquals(SyncStatus.SYNCED, decodedEntity.syncStatus)
    }

    @Test
    fun category_toDto_and_toEntity_roundtrip() {
        val entity = Category(
            id = "cat_food",
            name = "Food",
            icon = "restaurant",
            transactionType = "EXPENSE",
            isDefault = true,
            syncStatus = SyncStatus.PENDING_UPLOAD,
            updatedAt = 1778306200000L,
            isDeleted = false
        )

        val dto = entity.toDto(testUserId)
        assertEquals("cat_food", dto.id)
        assertEquals(testUserId, dto.userId)
        assertEquals("Food", dto.name)

        val decoded = dto.toEntity()
        assertEquals(entity.id, decoded.id)
        assertEquals(entity.name, decoded.name)
        assertEquals(SyncStatus.SYNCED, decoded.syncStatus)
    }

    @Test
    fun ledger_toDto_and_toEntity_roundtrip() {
        val entity = Ledger(
            id = "ledger_default",
            name = "Personal",
            currencyCode = "USD",
            balance = 120.0,
            totalIncome = 200.0,
            totalExpenses = 80.0,
            createdAt = 1778306000000L,
            updatedAt = 1778306200000L,
            isDefault = true,
            syncStatus = SyncStatus.PENDING_UPLOAD
        )

        val dto = entity.toDto(testUserId)
        assertEquals("ledger_default", dto.id)
        assertEquals(testUserId, dto.userId)
        assertEquals("Personal", dto.name)

        val decoded = dto.toEntity()
        assertEquals(entity.id, decoded.id)
        assertEquals(entity.name, decoded.name)
        assertEquals(SyncStatus.SYNCED, decoded.syncStatus)
    }
}
