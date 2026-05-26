package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.entity.Ledger
import kotlinx.coroutines.flow.Flow

interface LedgerRepository {
    fun getAllLedgers(): Flow<List<Ledger>>
    fun getLedgerById(id: String): Ledger?
    fun getDefaultLedger(): Ledger?
    suspend fun insertLedger(ledger: Ledger)
    suspend fun updateLedger(ledger: Ledger)
    suspend fun deleteLedger(id: String)
}