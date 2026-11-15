package com.example.expensetracker.data.sync

import com.example.expensetracker.data.local.SyncStatus
import com.example.expensetracker.data.local.UserPreferences
import com.example.expensetracker.data.local.dao.CategoryDao
import com.example.expensetracker.data.local.dao.LedgerDao
import com.example.expensetracker.data.local.dao.MonthlySnapshotDao
import com.example.expensetracker.data.local.dao.TransactionDao
import com.example.expensetracker.data.remote.dto.CategoryDto
import com.example.expensetracker.data.remote.dto.LedgerDto
import com.example.expensetracker.data.remote.dto.TransactionDto
import com.example.expensetracker.data.remote.mapper.toDto
import com.example.expensetracker.data.remote.mapper.toEntity
import com.example.expensetracker.data.remote.mapper.toEpochMillis
import com.example.expensetracker.domain.repository.AuthRepository
import com.example.expensetracker.domain.sync.SyncManager
import com.example.expensetracker.domain.sync.SyncResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn

@Singleton
class SyncManagerImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val authRepository: AuthRepository,
    private val ledgerDao: LedgerDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val monthlySnapshotDao: MonthlySnapshotDao,
    private val userPreferences: UserPreferences
) : SyncManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    override val lastSyncTime: StateFlow<Long?> = userPreferences.lastSyncTime
        .stateIn(scope, SharingStarted.Eagerly, null)

    override suspend fun sync(): SyncResult {
        val userId = authRepository.currentUserId
        if (!authRepository.isAuthenticated || userId == null) {
            return SyncResult.NotAuthenticated
        }

        if (_isSyncing.value) {
            return SyncResult.Success // Prevent overlapping sync passes
        }

        _isSyncing.value = true

        return try {
            // 1. PUSH PHASE: Sequential snapshot upload (Ledgers -> Categories -> Transactions)
            pushLedgers(userId)
            pushCategories(userId)
            pushTransactions(userId)

            // 2. PULL PHASE: Download cloud deltas
            pullLedgers(userId)
            pullCategories(userId)
            pullTransactions(userId)

            // 3. DERIVED STATE RECALCULATION PASS
            val ledgers = ledgerDao.getAllLedgers().firstOrNull() ?: emptyList()
            for (ledger in ledgers) {
                transactionDao.recalculateDerivedState(ledger.id, ledgerDao, monthlySnapshotDao)
            }

            // 4. RECORD SUCCESSFUL SYNC TIME
            userPreferences.setLastSyncTime(System.currentTimeMillis())

            SyncResult.Success
        } catch (e: Exception) {
            SyncResult.Error(e)
        } finally {
            _isSyncing.value = false
        }
    }

    private suspend fun pushLedgers(userId: String) {
        val dirty = ledgerDao.getPendingSyncLedgers()
        for (ledger in dirty) {
            val dto = ledger.toDto(userId)
            supabase.postgrest["ledgers"].upsert(dto)
            ledgerDao.markAsSynced(ledger.id, dto.id, ledger.updatedAt)
        }
    }

    private suspend fun pushCategories(userId: String) {
        val dirty = categoryDao.getPendingSyncCategories()
        for (category in dirty) {
            val dto = category.toDto(userId)
            supabase.postgrest["categories"].upsert(dto)
            categoryDao.markAsSynced(category.id, dto.id, category.updatedAt)
        }
    }

    private suspend fun pushTransactions(userId: String) {
        val dirty = transactionDao.getPendingSyncTransactions()
        for (tx in dirty) {
            val dto = tx.toDto(userId)
            supabase.postgrest["transactions"].upsert(dto)
            transactionDao.markAsSynced(tx.id, dto.id, tx.updatedAt)
        }
    }

    private suspend fun pullLedgers(userId: String) {
        val remoteList = supabase.postgrest["ledgers"].select {
            filter {
                eq("user_id", userId)
            }
        }.decodeList<LedgerDto>()

        for (dto in remoteList) {
            val local = ledgerDao.getLedgerById(dto.id)
            val remoteUpdated = dto.updatedAt.toEpochMillis()
            if (local == null || (local.syncStatus == SyncStatus.SYNCED && remoteUpdated > local.updatedAt)) {
                ledgerDao.upsert(dto.toEntity())
            }
        }
    }

    private suspend fun pullCategories(userId: String) {
        val remoteList = supabase.postgrest["categories"].select {
            filter {
                eq("user_id", userId)
            }
        }.decodeList<CategoryDto>()

        for (dto in remoteList) {
            categoryDao.upsert(dto.toEntity())
        }
    }

    private suspend fun pullTransactions(userId: String) {
        val remoteList = supabase.postgrest["transactions"].select {
            filter {
                eq("user_id", userId)
            }
        }.decodeList<TransactionDto>()

        for (dto in remoteList) {
            val local = transactionDao.getTransactionById(dto.id)
            val remoteUpdated = dto.updatedAt.toEpochMillis()
            if (local == null || (local.syncStatus == SyncStatus.SYNCED && remoteUpdated > local.updatedAt)) {
                transactionDao.upsert(dto.toEntity())
            }
        }
    }
}
