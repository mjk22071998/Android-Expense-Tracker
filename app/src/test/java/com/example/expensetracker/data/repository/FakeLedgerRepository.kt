package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.domain.model.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeLedgerRepository : LedgerRepository {

    private val defaultLedgerFlow = MutableStateFlow<Ledger?>(null)

    fun emitDefaultLedger(ledger: Ledger?) {
        defaultLedgerFlow.value = ledger
    }

    override fun getAllLedgers(): Flow<List<Ledger>> =
        TODO("Not needed for current DashboardViewModel tests")

    override fun getDefaultLedger(): Flow<Ledger?> = defaultLedgerFlow

    override suspend fun getLedgerById(id: String): AppResult<Ledger?> =
        TODO("Not needed for current DashboardViewModel tests")

    override suspend fun insertLedger(ledger: Ledger): AppResult<Unit> =
        TODO("Not needed for current DashboardViewModel tests")

    override suspend fun updateLedger(ledger: Ledger): AppResult<Unit> =
        TODO("Not needed for current DashboardViewModel tests")

    override suspend fun deleteLedger(id: String): AppResult<Unit> =
        TODO("Not needed for current DashboardViewModel tests")
}