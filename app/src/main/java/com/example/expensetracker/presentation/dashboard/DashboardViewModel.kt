package com.example.expensetracker.presentation.dashboard

import android.content.Context
import com.example.expensetracker.data.local.UserPreferences
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.data.repository.LedgerRepository
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.domain.model.AppResult
import com.example.expensetracker.domain.model.CurrencyHelper
import com.example.expensetracker.domain.model.DateRangeFilter
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
import java.util.Calendar

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val ledgerRepository: LedgerRepository,
    private val userPreferences: UserPreferences,
    @param:ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var currentLedgerId: String = ""

    init {
        loadDefaultLedger()
        loadCurrencySymbol()
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
                        currentLedgerId = it.id
                        _uiState.update { state ->
                            state.copy(
                                balance = ledger.balance,
                                totalIncome = ledger.totalIncome,
                                totalExpense = ledger.totalExpenses
                            )
                        }
                        loadTransactions()
                    }
                }
        }
    }

    private fun loadTransactions() {
        val (startDate, endDate) = getDateRange(_uiState.value.selectedFilter)
        launchSafely(onError = ::handleError) {
            transactionRepository.getTransactions(
                ledgerId = currentLedgerId,
                startDate = startDate,
                endDate = endDate
            )
                .catch { e -> handleError(e.toUiError()) }
                .collect { transactions ->
                    _uiState.update { state ->
                        state.copy(transactions = transactions)
                    }
                }
        }
    }

    fun onFilterSelected(filter: DateRangeFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        loadTransactions()
    }

    fun softDeleteTransaction(transaction: TransactionWithCategory) {
        launchSafely(onError = ::handleError) {
            val result = transactionRepository.softDeleteTransaction(
                Transaction(
                    id = transaction.id,
                    ledgerId = transaction.ledgerId,
                    categoryId = transaction.categoryId,
                    amount = transaction.amount,
                    type = transaction.type,
                    note = transaction.note,
                    date = transaction.date,
                    createdAt = transaction.createdAt,
                    updatedAt = System.currentTimeMillis(),
                    isDeleted = true
                )
            )
            when (result) {
                is AppResult.Success -> Unit // list updates on its own via the Flow re-emitting
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

    private fun getDateRange(filter: DateRangeFilter): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        return when (filter) {
            is DateRangeFilter.Last30Days -> {
                val start = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -30)
                }.timeInMillis
                Pair(start, now)
            }
            is DateRangeFilter.ThisWeek -> {
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                Pair(calendar.timeInMillis, now)
            }
            is DateRangeFilter.ThisMonth -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                Pair(calendar.timeInMillis, now)
            }
            is DateRangeFilter.ThisYear -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                Pair(calendar.timeInMillis, now)
            }
            is DateRangeFilter.Custom -> {
                Pair(filter.startDate, filter.endDate)
            }
        }
    }
}