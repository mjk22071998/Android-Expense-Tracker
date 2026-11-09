package com.example.expensetracker.presentation.transaction

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.UserPreferences
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.LedgerRepository
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.domain.model.AppResult
import com.example.expensetracker.domain.model.CurrencyHelper
import com.example.expensetracker.domain.model.UiError
import com.example.expensetracker.domain.model.toUiError
import com.example.expensetracker.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import java.util.UUID

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val ledgerRepository: LedgerRepository,
    private val userPreferences: UserPreferences,
    @param:ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AddEditTransactionUiState())
    val uiState: StateFlow<AddEditTransactionUiState> = _uiState.asStateFlow()

    private val transactionId: String? =
        savedStateHandle[Constants.NavArgs.TRANSACTION_ID]

    init {
        _uiState.update { it.copy(isEditMode = transactionId != null) }
        loadCategories()
        loadDefaultLedger()
        loadCurrencySymbol()
        transactionId?.let { loadTransactionForEdit(it) }
    }

    private fun loadCurrencySymbol() {
        launchSafely(onError = ::handleError) {
            userPreferences.currencyCode
                .catch { e -> handleError(e.toUiError()) }
                .collect { code ->
                    _uiState.update { state ->
                        state.copy(currencySymbol = CurrencyHelper.getSymbol(code, context))
                    }
                }
        }
    }

    private fun loadDefaultLedger() {
        launchSafely(onError = ::handleError) {
            ledgerRepository.getDefaultLedger()
                .catch { e -> handleError(e.toUiError()) }
                .collect { ledger ->
                    ledger?.let {
                        _uiState.update { state -> state.copy(currentLedgerId = it.id) }
                        validate()
                    }
                }
        }
    }

    private fun loadCategories() {
        launchSafely(onError = ::handleError) {
            categoryRepository.getAllCategories()
                .catch { e -> handleError(e.toUiError()) }
                .collect { categories ->
                    _uiState.update { state ->
                        state.copy(
                            allCategories = categories,
                            filteredCategories = categories.filter { it.transactionType == state.type }
                        )
                    }
                }
        }
    }

    private fun loadTransactionForEdit(id: String) {
        launchSafely(onError = ::handleError) {
            when (val result = transactionRepository.getTransactionById(id)) {
                is AppResult.Success -> {
                    val transaction = result.data
                    if (transaction == null) {
                        handleError(UiError.NotFound())
                        return@launchSafely
                    }
                    _uiState.update { state ->
                        state.copy(
                            amount = transaction.amount.toString(),
                            type = transaction.type,
                            selectedCategoryId = transaction.categoryId,
                            note = transaction.note,
                            date = transaction.date,
                            originalTransaction = transaction,
                            filteredCategories = state.allCategories.filter { cat ->
                                cat.transactionType == transaction.type
                            }
                        )
                    }
                    validate()
                }
                is AppResult.Error -> handleError(result.error)
            }
        }
    }

    fun onTypeChanged(type: String) {
        _uiState.update { state ->
            state.copy(
                type = type,
                selectedCategoryId = null,
                filteredCategories = state.allCategories.filter { it.transactionType == type }
            )
        }
        validate()
    }

    fun onAmountChanged(value: String) {
        val trimmed = value.trim()
        val hasInvalidChars = trimmed.startsWith("-") || trimmed.any { !it.isDigit() && it != '.' }
        val hasMultipleDots = trimmed.count { it == '.' } > 1
        val filtered = if (hasInvalidChars || hasMultipleDots) {
            ""
        } else {
            trimmed
        }
        _uiState.update { it.copy(amount = filtered) }
        validate()
    }

    fun onCategorySelected(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        validate()
    }

    fun onNoteChanged(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    fun onDateChanged(date: Long) {
        _uiState.update { it.copy(date = date) }
    }

    private fun validate() {
        val state = _uiState.value
        val amountValue = state.amount.toDoubleOrNull() ?: 0.0
        _uiState.update {
            it.copy(
                isSaveEnabled = amountValue > 0.0 &&
                    amountValue.isFinite() &&
                    it.selectedCategoryId != null &&
                    it.currentLedgerId.isNotBlank()
            )
        }
    }

    fun saveTransaction() {
        val state = _uiState.value
        if (state.currentLedgerId.isBlank()) return
        val amountValue = state.amount.toDoubleOrNull() ?: return
        if (!amountValue.isFinite() || amountValue <= 0.0) return
        val now = System.currentTimeMillis()

        launchSafely(onError = ::handleError) {
            _uiState.update { it.copy(isLoading = true) }

            val result = if (state.isEditMode && state.originalTransaction != null) {
                val updatedTransaction = state.originalTransaction.copy(
                    amount = amountValue,
                    type = state.type,
                    categoryId = state.selectedCategoryId,
                    note = state.note,
                    date = state.date,
                    updatedAt = now
                )
                transactionRepository.updateTransaction(
                    oldTransaction = state.originalTransaction,
                    newTransaction = updatedTransaction
                )
            } else {
                val newTransaction = Transaction.create(
                    amount = amountValue,
                    type = state.type,
                    categoryId = state.selectedCategoryId,
                    ledgerId = state.currentLedgerId,
                    note = state.note,
                    date = state.date,
                    timestamp = now
                )
                transactionRepository.insertTransaction(newTransaction)
            }

            when (result) {
                is AppResult.Success -> _uiState.update { it.copy(isLoading = false, isSaved = true) }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    handleError(result.error)
                }
            }
        }
    }

    fun addCategory(name: String, icon: String) {
        launchSafely(onError = ::handleError) {
            val newCategory = Category.create(
                name = name,
                icon = icon,
                transactionType = _uiState.value.type
            )
            when (val result = categoryRepository.insertCategory(newCategory)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(selectedCategoryId = newCategory.id) }
                    validate()
                }
                is AppResult.Error -> handleError(result.error)
            }
        }
    }

    fun updateCategory(category: Category, name: String, icon: String) {
        launchSafely(onError = ::handleError) {
            val result = categoryRepository.updateCategory(category.copy(name = name, icon = icon))
            if (result is AppResult.Error) handleError(result.error)
        }
    }

    fun deleteCategory(category: Category) {
        launchSafely(onError = ::handleError) {
            when (val result = categoryRepository.deleteCategory(category.id)) {
                is AppResult.Success -> {
                    if (_uiState.value.selectedCategoryId == category.id) {
                        _uiState.update { it.copy(selectedCategoryId = null) }
                        validate()
                    }
                }
                is AppResult.Error -> handleError(result.error)
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