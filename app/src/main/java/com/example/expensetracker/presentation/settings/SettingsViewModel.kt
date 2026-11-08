package com.example.expensetracker.presentation.settings

import android.content.Context
import com.example.expensetracker.data.local.UserPreferences
import com.example.expensetracker.data.repository.LedgerRepository
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.domain.model.AppResult
import com.example.expensetracker.domain.model.CurrencyHelper
import com.example.expensetracker.domain.model.UiError
import com.example.expensetracker.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val transactionRepository: TransactionRepository,
    private val ledgerRepository: LedgerRepository,
    @param:ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
        loadCurrencies()
    }

    private fun observePreferences() {
        launchSafely(onError = ::handleError) {
            userPreferences.currencyCode.collect { code ->
                _uiState.update { state ->
                    state.copy(
                        currencyCode = code,
                        currencySymbol = CurrencyHelper.getSymbol(code, context)
                    )
                }
            }
        }
        launchSafely(onError = ::handleError) {
            userPreferences.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
    }

    private fun loadCurrencies() {
        _uiState.update { it.copy(filteredCurrencies = CurrencyHelper.allCurrencies) }
    }

    fun onCurrencySearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                currencySearchQuery = query,
                filteredCurrencies = CurrencyHelper.search(query)
            )
        }
    }

    fun onCurrencySelected(currencyCode: String) {
        launchSafely(onError = ::handleError) {
            userPreferences.setCurrencyCode(currencyCode)
            _uiState.update { state ->
                state.copy(
                    isCurrencyPickerVisible = false,
                    currencySearchQuery = ""
                )
            }
        }
    }

    fun onCurrencyPickerVisibilityChanged(isVisible: Boolean) {
        _uiState.update { state ->
            state.copy(
                isCurrencyPickerVisible = isVisible,
                currencySearchQuery = if (!isVisible) "" else state.currencySearchQuery,
                filteredCurrencies = if (!isVisible) CurrencyHelper.allCurrencies
                else state.filteredCurrencies
            )
        }
    }

    fun setThemeMode(mode: String) {
        launchSafely(onError = ::handleError) {
            userPreferences.setThemeMode(mode)
        }
    }

    fun clearAllTransactions(onSuccess: () -> Unit = {}) {
        launchSafely(onError = ::handleError) {
            val defaultLedger = ledgerRepository.getDefaultLedger().first()
            val ledgerId = defaultLedger?.id ?: return@launchSafely
            val result = transactionRepository.clearAllTransactions(ledgerId)
            if (result is AppResult.Error) {
                handleError(result.error)
            } else {
                onSuccess()
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun handleError(error: UiError) {
        _uiState.update { it.copy(error = error) }
    }
}