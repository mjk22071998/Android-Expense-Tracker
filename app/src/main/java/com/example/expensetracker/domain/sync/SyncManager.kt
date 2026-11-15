package com.example.expensetracker.domain.sync

import kotlinx.coroutines.flow.StateFlow

sealed interface SyncResult {
    data object Success : SyncResult
    data class Error(val throwable: Throwable) : SyncResult
    data object NotAuthenticated : SyncResult
}

interface SyncManager {
    val isSyncing: StateFlow<Boolean>
    val lastSyncTime: StateFlow<Long?>

    suspend fun sync(): SyncResult
}
