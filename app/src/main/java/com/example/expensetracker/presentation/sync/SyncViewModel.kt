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
    val error: String? = null,
    val success: String? = null,
    val passwordResetSent: Boolean = false,
    val passwordUpdateSuccess: Boolean = false
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
                    authErrorMessage = promptState.error,
                    authSuccessMessage = promptState.success,
                    passwordResetSent = promptState.passwordResetSent,
                    passwordUpdateSuccess = promptState.passwordUpdateSuccess
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authPromptState.value = AuthPromptState(isLoading = true, error = null, success = null)
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
            authPromptState.value = AuthPromptState(isLoading = true, error = null, success = null)
            val result = authRepository.signUpWithEmail(email, password)
            result.onSuccess {
                authPromptState.value = AuthPromptState(isLoading = false, error = null)
                syncScheduler.enqueueOneTimeSync()
            }.onFailure { error ->
                authPromptState.value = AuthPromptState(isLoading = false, error = error.message ?: "Registration failed")
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            authPromptState.value = authPromptState.value.copy(isLoading = true, error = null, success = null)
            val result = authRepository.sendPasswordResetEmail(email)
            result.onSuccess {
                authPromptState.value = authPromptState.value.copy(
                    isLoading = false,
                    passwordResetSent = true,
                    success = "Verification code sent to $email"
                )
            }.onFailure { error ->
                authPromptState.value = authPromptState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to send reset code"
                )
            }
        }
    }

    fun resetPasswordWithOtp(email: String, otp: String, newPassword: String) {
        viewModelScope.launch {
            authPromptState.value = authPromptState.value.copy(isLoading = true, error = null, success = null)
            val result = authRepository.resetPasswordWithOtp(email, otp, newPassword)
            result.onSuccess {
                authPromptState.value = AuthPromptState(
                    isLoading = false,
                    error = null,
                    success = "Password reset successfully. Signed in!"
                )
                syncScheduler.enqueueOneTimeSync()
            }.onFailure { error ->
                authPromptState.value = authPromptState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to reset password. Please check the code."
                )
            }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            authPromptState.value = authPromptState.value.copy(
                isLoading = true,
                error = null,
                success = null,
                passwordUpdateSuccess = false
            )
            val result = authRepository.updatePassword(currentPassword, newPassword)
            result.onSuccess {
                authPromptState.value = authPromptState.value.copy(
                    isLoading = false,
                    passwordUpdateSuccess = true,
                    success = "Password updated successfully!"
                )
            }.onFailure { error ->
                authPromptState.value = authPromptState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to update password. Verify your current password."
                )
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

    fun clearAuthMessages() {
        authPromptState.value = authPromptState.value.copy(error = null, success = null)
    }

    fun resetPasswordFlowState() {
        authPromptState.value = authPromptState.value.copy(
            passwordResetSent = false,
            passwordUpdateSuccess = false,
            error = null,
            success = null
        )
    }
}
