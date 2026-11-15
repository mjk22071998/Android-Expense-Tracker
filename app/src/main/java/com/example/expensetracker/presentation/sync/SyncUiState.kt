package com.example.expensetracker.presentation.sync

data class SyncUiState(
    val isAuthenticated: Boolean = false,
    val userEmail: String? = null,
    val isSyncing: Boolean = false,
    val lastSyncedTimeMillis: Long? = null,
    val pendingUploadCount: Int = 0,
    val isAuthLoading: Boolean = false,
    val authErrorMessage: String? = null
)
