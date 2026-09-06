package com.example.expensetracker.presentation.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.UserPreferences
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.LedgerRepository
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.domain.model.CurrencyHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val ledgerRepository: LedgerRepository,
    private val userPreferences: UserPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel(){
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
        viewModelScope.launch {
            userPreferences.currencyCode.collect { code ->
                _uiState.update { state ->
                    state.copy(currencySymbol = CurrencyHelper.getSymbol(code))
                }
            }
        }
    }

    private fun loadDefaultLedger() {
        viewModelScope.launch {
            ledgerRepository.getDefaultLedger().collect { ledger ->
                ledger?.let {
                    _uiState.update { state -> state.copy(currentLedgerId = it.id) }
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
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
        viewModelScope.launch {
            val transaction = transactionRepository.getTransactionById(id)
            transaction?.let {
                _uiState.update { state ->
                    state.copy(
                        amount = it.amount.toString(),
                        type = it.type,
                        selectedCategoryId = it.categoryId,
                        note = it.note,
                        date = it.date,
                        originalTransaction = it,
                        filteredCategories = state.allCategories.filter { cat ->
                            cat.transactionType == it.type
                        }
                    )
                }
                validate()
            }
        }
    }

    fun onTypeChanged(type: String) {
        _uiState.update { state ->
            state.copy(
                type = type,
                selectedCategoryId = null, // reset category since list changes
                filteredCategories = state.allCategories.filter { it.transactionType == type }
            )
        }
        validate()
    }

    fun onAmountChanged(value: String) {
        // allow only digits and a single decimal point
        val filtered = value.filterIndexed { index, c ->
            c.isDigit() || (c == '.' && !value.take(index).contains('.'))
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
            it.copy(isSaveEnabled = amountValue > 0.0 && it.selectedCategoryId != null)
        }
    }

    fun saveTransaction() {
        val state = _uiState.value
        val amountValue = state.amount.toDoubleOrNull() ?: return
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            if (state.isEditMode && state.originalTransaction != null) {
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
                val newTransaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    ledgerId = state.currentLedgerId,
                    categoryId = state.selectedCategoryId,
                    amount = amountValue,
                    type = state.type,
                    note = state.note,
                    date = state.date,
                    createdAt = now,
                    updatedAt = now
                )
                transactionRepository.insertTransaction(newTransaction)
            }

            _uiState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }
}