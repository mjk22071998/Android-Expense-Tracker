package com.example.expensetracker.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.dao.TransactionDao
import com.example.expensetracker.domain.repository.AuthRepository
import com.example.expensetracker.domain.sync.SyncManager
import com.example.expensetracker.worker.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private data class AuthPromptState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager,
    private val syncScheduler: SyncScheduler,
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val authPromptState = MutableStateFlow(AuthPromptState())

    private val _uiState = MutableStateFlow(
        SyncUiState(
            isAuthenticated = authRepository.isAuthenticated,
            userEmail = authRepository.currentUserFlow.value?.email,
            isSyncing = syncManager.isSyncing.value,
            lastSyncedTimeMillis = syncManager.lastSyncTime.value
        )
    )
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                authRepository.currentUserFlow,
                syncManager.isSyncing,
                syncManager.lastSyncTime,
                transactionDao.getPendingSyncCountFlow(),
                authPromptState
            ) { currentUser, isSyncing, lastSyncTime, pendingCount, promptState ->
                SyncUiState(
                    isAuthenticated = currentUser != null,
                    userEmail = currentUser?.email,
                    isSyncing = isSyncing,
                    lastSyncedTimeMillis = lastSyncTime,
                    pendingUploadCount = pendingCount,
                    isAuthLoading = promptState.isLoading,
                    authErrorMessage = promptState.error
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authPromptState.value = AuthPromptState(isLoading = true, error = null)
            val result = authRepository.signInWithEmail(email, password)
            result.onSuccess {
                authPromptState.value = AuthPromptState(isLoading = false, error = null)
                syncScheduler.enqueueOneTimeSync()
            }.onFailure { error ->
                authPromptState.value = AuthPromptState(isLoading = false, error = error.message ?: "Authentication failed")
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            authPromptState.value = AuthPromptState(isLoading = true, error = null)
            val result = authRepository.signUpWithEmail(email, password)
            result.onSuccess {
                authPromptState.value = AuthPromptState(isLoading = false, error = null)
                syncScheduler.enqueueOneTimeSync()
            }.onFailure { error ->
                authPromptState.value = AuthPromptState(isLoading = false, error = error.message ?: "Registration failed")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun syncNow() {
        syncScheduler.enqueueOneTimeSync()
    }

    fun clearAuthError() {
        authPromptState.value = authPromptState.value.copy(error = null)
    }
}
