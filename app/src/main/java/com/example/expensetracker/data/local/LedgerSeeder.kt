package com.example.expensetracker.data.local

import android.content.Context
import com.example.expensetracker.data.local.dao.LedgerDao
import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.domain.model.CurrencyHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class LedgerSeeder @Inject constructor(
    private val ledgerDao: LedgerDao,
    @param:ApplicationContext private val context: Context
) {
    suspend fun seedIfNeeded() {
        val existingDefault = ledgerDao.getDefaultLedgerSync()
        if (existingDefault == null) {
            val now = System.currentTimeMillis()
            val defaultCurrency = CurrencyHelper.getDefaultCurrencyCode(context)

            ledgerDao.insert(
                Ledger.create(
                    id = "ledger_default",
                    name = "Personal",
                    currencyCode = defaultCurrency,
                    isDefault = true,
                    timestamp = now
                )
            )
        }
    }
}
