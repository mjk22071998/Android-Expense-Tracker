package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.dao.LedgerDao
import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.domain.model.AppResult
import com.example.expensetracker.domain.model.safeCall
import jakarta.inject.Inject

class LedgerRepositoryImpl @Inject constructor(
    private val ledgerDao: LedgerDao
) : LedgerRepository {

    override fun getAllLedgers() = ledgerDao.getAllLedgers()

    override fun getDefaultLedger() = ledgerDao.getDefaultLedger()

    override suspend fun getLedgerById(id: String): AppResult<Ledger?> = safeCall {
        ledgerDao.getLedgerById(id)
    }

    override suspend fun insertLedger(ledger: Ledger): AppResult<Unit> = safeCall {
        ledgerDao.insert(ledger)
    }

    override suspend fun updateLedger(ledger: Ledger): AppResult<Unit> = safeCall {
        ledgerDao.update(ledger)
    }

    override suspend fun deleteLedger(id: String): AppResult<Unit> = safeCall {
        ledgerDao.delete(id)
    }
}