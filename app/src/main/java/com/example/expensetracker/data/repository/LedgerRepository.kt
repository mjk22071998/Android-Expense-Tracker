package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.domain.model.AppResult
import kotlinx.coroutines.flow.Flow

interface LedgerRepository {
    fun getAllLedgers(): Flow<List<Ledger>>
    fun getDefaultLedger(): Flow<Ledger?>

    suspend fun getLedgerById(id: String): AppResult<Ledger?>
    suspend fun insertLedger(ledger: Ledger): AppResult<Unit>
    suspend fun updateLedger(ledger: Ledger): AppResult<Unit>
    suspend fun deleteLedger(id: String): AppResult<Unit>
}