package com.example.expensetracker.presentation.dashboard

import android.content.Context
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.UserPreferences
import com.example.expensetracker.data.local.entity.Transaction
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.LedgerRepository
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.domain.model.AppResult
import com.example.expensetracker.domain.model.CurrencyHelper
import com.example.expensetracker.domain.model.DateRangeFilter
import com.example.expensetracker.domain.model.DateUtils
import com.example.expensetracker.domain.model.UiError
import com.example.expensetracker.domain.model.toUiError
import com.example.expensetracker.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import java.util.Calendar

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val ledgerRepository: LedgerRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferences: UserPreferences,
    @param:ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var currentLedgerId: String = ""
    private var transactionsJob: Job? = null
    private val searchQueryFlow = MutableStateFlow("")

    init {
        loadDefaultLedger()
        loadCurrencySymbol()
        loadCategories()
        observeSearchQuery()
    }

    // ---------- loading ----------

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

    private fun loadCategories() {
        launchSafely(onError = ::handleError) {
            categoryRepository.getAllCategories()
                .catch { e -> handleError(e.toUiError()) }
                .collect { categories ->
                    _uiState.update { it.copy(allCategories = categories) }
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

    private fun observeSearchQuery() {
        launchSafely(onError = ::handleError) {
            searchQueryFlow
                .debounce(Constants.Search.DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest {
                    loadTransactions()
                }
        }
    }

    private fun loadTransactions() {
        transactionsJob?.cancel() // stop the previous listener before starting a new one

        val (startDate, endDate) = getDateRange(_uiState.value.selectedFilter)
        val query = _uiState.value.searchQuery
        val typeFilter = _uiState.value.selectedTypeFilter
        val categoryIds = _uiState.value.selectedCategoryIds

        transactionsJob = launchSafely(onError = ::handleError) {
            transactionRepository.getTransactions(
                ledgerId = currentLedgerId,
                startDate = startDate,
                endDate = endDate,
                searchQuery = query,
                typeFilter = typeFilter,
                categoryIds = categoryIds
            )
                .catch { e -> handleError(e.toUiError()) }
                .collect { transactions ->
                    _uiState.update { state -> state.copy(transactions = transactions) }
                }
        }
    }

    // ---------- date and type filters ----------

    fun onFilterSelected(filter: DateRangeFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        loadTransactions()
    }

    fun onTypeFilterSelected(type: String) {
        _uiState.update { state ->
            state.copy(selectedTypeFilter = if (state.selectedTypeFilter == type) null else type)
        }
        loadTransactions()
    }

    // ---------- search ----------

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) } // instant: what the text field shows
        searchQueryFlow.value = query                     // debounced: what triggers the database query
    }

    fun onSearchActivated() {
        _uiState.update { it.copy(isSearchActive = true) }
    }

    fun onSearchClosed() {
        _uiState.update { it.copy(isSearchActive = false, searchQuery = "") }
        searchQueryFlow.value = ""
        loadTransactions()
    }

    // ---------- category filter ----------

    fun onCategoryFilterToggled(categoryId: String) {
        _uiState.update { state ->
            val updated = if (categoryId in state.selectedCategoryIds) {
                state.selectedCategoryIds - categoryId
            } else {
                state.selectedCategoryIds + categoryId
            }
            state.copy(selectedCategoryIds = updated)
        }
    }

    fun onCategoryFilterSheetVisibilityChanged(isVisible: Boolean) {
        _uiState.update { it.copy(isCategoryFilterSheetVisible = isVisible) }
    }

    fun onApplyCategoryFilters() {
        _uiState.update { it.copy(isCategoryFilterSheetVisible = false) }
        loadTransactions()
    }

    fun onClearCategoryFilters() {
        _uiState.update {
            it.copy(selectedCategoryIds = emptySet(), isCategoryFilterSheetVisible = false)
        }
        loadTransactions()
    }

    // ---------- delete ----------

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
            if (result is AppResult.Error) handleError(result.error)
        }
    }

    // ---------- errors ----------

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun handleError(error: UiError) {
        _uiState.update { it.copy(error = error) }
    }

    // ---------- date range ----------

    private fun getDateRange(filter: DateRangeFilter): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        return when (filter) {
            is DateRangeFilter.Today -> DateUtils.todayLocalRange()
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
            is DateRangeFilter.Custom -> Pair(filter.startDate, filter.endDate)
        }
    }
}