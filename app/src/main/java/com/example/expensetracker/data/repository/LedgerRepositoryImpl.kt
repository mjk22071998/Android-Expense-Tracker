package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.dao.LedgerDao
import com.example.expensetracker.data.local.entity.Ledger
import jakarta.inject.Inject

class LedgerRepositoryImpl @Inject constructor(
    private val ledgerDao: LedgerDao
) : LedgerRepository {
    override fun getAllLedgers() = ledgerDao.getAllLedgers()

    override fun getLedgerById(id: String) = ledgerDao.getLedgerById(id)

    override fun getDefaultLedger() = ledgerDao.getDefaultLedger()

    override suspend fun insertLedger(ledger: Ledger) = ledgerDao.insert(ledger)

    override suspend fun updateLedger(ledger: Ledger) = ledgerDao.update(ledger)

    override suspend fun deleteLedger(id: String) = ledgerDao.delete(id)
}