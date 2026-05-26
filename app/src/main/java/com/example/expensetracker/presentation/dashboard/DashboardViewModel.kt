package com.example.expensetracker.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.LedgerRepository
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.domain.model.DateRangeFilter
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val ledgerRepository: LedgerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var currentLedgerId: String = ""

    init {
        loadDefaultLedger()
    }

    private fun loadDefaultLedger() {
        viewModelScope.launch {
            ledgerRepository.getDefaultLedger().collect { ledger ->
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
        viewModelScope.launch {
            transactionRepository.getTransactions(
                ledgerId = currentLedgerId,
                startDate = startDate,
                endDate = endDate
            ).collect { transactions ->
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

    private fun getDateRange(filter: DateRangeFilter): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        return when (filter) {
            is DateRangeFilter.Last30Days -> {
                val start = calendar.apply {
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