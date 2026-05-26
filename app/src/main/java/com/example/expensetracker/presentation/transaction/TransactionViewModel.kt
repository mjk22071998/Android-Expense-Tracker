package com.example.expensetracker.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.LedgerRepository
import com.example.expensetracker.data.repository.TransactionRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class TransactionViewModel@Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val ledgerRepository: LedgerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun insertTransaction(
        amount: Double,
        type: String,
        categoryId: String?,
        note: String,
        date: Long
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                ledgerId = _uiState.value.currentLedgerId,
                categoryId = categoryId,
                amount = amount,
                type = type,
                note = note,
                date = date,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            transactionRepository.insertTransaction(transaction)
        }
    }

    fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(oldTransaction, newTransaction)
        }
    }

    fun softDeleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.softDeleteTransaction(transaction)
        }
    }
}