package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.domain.model.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeLedgerRepository : LedgerRepository {

    private val ledgersFlow = MutableStateFlow<List<Ledger>>(emptyList())

    fun emitDefaultLedger(ledger: Ledger?) {
        if (ledger != null) {
            val current = ledgersFlow.value.filter { it.id != ledger.id }
            ledgersFlow.value = current + ledger
        } else {
            ledgersFlow.value = ledgersFlow.value.filter { !it.isDefault }
        }
    }

    fun emitLedgers(list: List<Ledger>) {
        ledgersFlow.value = list
    }

    override fun getAllLedgers(): Flow<List<Ledger>> = ledgersFlow.map { list ->
        list.filter { !it.isDeleted }
    }

    override fun getDefaultLedger(): Flow<Ledger?> = ledgersFlow.map { list ->
        list.firstOrNull { it.isDefault && !it.isDeleted }
    }

    override suspend fun getLedgerById(id: String): AppResult<Ledger?> {
        val found = ledgersFlow.value.firstOrNull { it.id == id && !it.isDeleted }
        return AppResult.Success(found)
    }

    override suspend fun insertLedger(ledger: Ledger): AppResult<Unit> {
        ledgersFlow.value = ledgersFlow.value + ledger
        return AppResult.Success(Unit)
    }

    override suspend fun updateLedger(ledger: Ledger): AppResult<Unit> {
        ledgersFlow.value = ledgersFlow.value.map { if (it.id == ledger.id) ledger else it }
        return AppResult.Success(Unit)
    }

    override suspend fun deleteLedger(id: String): AppResult<Unit> {
        ledgersFlow.value = ledgersFlow.value.filter { it.id != id }
        return AppResult.Success(Unit)
    }

    override suspend fun softDeleteLedger(id: String): AppResult<Unit> {
        ledgersFlow.value = ledgersFlow.value.map { if (it.id == id) it.copy(isDeleted = true) else it }
        return AppResult.Success(Unit)
    }
}
