package com.example.expensetracker.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.UserPreferences
import com.example.expensetracker.domain.model.CurrencyHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
        loadCurrencies()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferences.currencyCode.collect { code ->
                _uiState.update { state ->
                    state.copy(
                        currencyCode = code,
                        currencySymbol = CurrencyHelper.getSymbol(code)
                    )
                }
            }
        }
        viewModelScope.launch {
            userPreferences.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
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
        viewModelScope.launch {
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

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkTheme(isDark)
        }
    }
}