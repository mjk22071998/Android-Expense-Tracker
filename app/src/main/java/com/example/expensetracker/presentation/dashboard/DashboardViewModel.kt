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
import com.example.expensetracker.domain.usecase.PdfReportGenerator
import com.example.expensetracker.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

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
    private var monthlyComparisonJob: Job? = null
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
                        val ledgerChanged = currentLedgerId != it.id
                        currentLedgerId = it.id
                        _uiState.update { state ->
                            state.copy(
                                balance = ledger.balance,
                                totalIncome = ledger.totalIncome,
                                totalExpense = ledger.totalExpenses
                            )
                        }
                        if (ledgerChanged) {
                            loadTransactions()
                            loadMonthlyComparison()
                        }
                    }
                }
        }
    }

    private fun loadMonthlyComparison() {
        monthlyComparisonJob?.cancel()
        if (currentLedgerId.isEmpty()) return

        val (thisMonthStart, thisMonthEnd) = DateUtils.thisMonthRange()
        val (lastMonthStart, lastMonthEnd) = DateUtils.lastMonthRange()

        monthlyComparisonJob = launchSafely(onError = ::handleError) {
            combine(
                transactionRepository.getPeriodIncome(currentLedgerId, thisMonthStart, thisMonthEnd),
                transactionRepository.getPeriodExpense(currentLedgerId, thisMonthStart, thisMonthEnd),
                transactionRepository.getPeriodIncome(currentLedgerId, lastMonthStart, lastMonthEnd),
                transactionRepository.getPeriodExpense(currentLedgerId, lastMonthStart, lastMonthEnd)
            ) { thisInc, thisExp, lastInc, lastExp ->
                val thisIncome = thisInc ?: 0.0
                val thisExpense = thisExp ?: 0.0
                val lastIncome = lastInc ?: 0.0
                val lastExpense = lastExp ?: 0.0

                val hasLastMonth = (lastIncome > 0.0 || lastExpense > 0.0)
                val thisNet = thisIncome - thisExpense
                val lastNet = lastIncome - lastExpense
                val delta = if (hasLastMonth) thisNet - lastNet else null

                Triple(delta, hasLastMonth, thisNet)
            }.collect { (delta, hasLastMonth, thisNet) ->
                _uiState.update { state ->
                    state.copy(
                        monthlyTrendDelta = delta,
                        hasLastMonthData = hasLastMonth,
                        thisMonthNet = thisNet
                    )
                }
            }
        }
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
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

        val (startDate, endDate) = _uiState.value.selectedFilter.calculateRange()
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
                transaction.toTransaction().toSoftDeleted()
            )
            if (result is AppResult.Error) handleError(result.error)
        }
    }

    // ---------- PDF Export ----------

    fun generatePdfReport(
        filter: DateRangeFilter,
        typeFilter: String?,
        categoryIds: Set<String>,
        onComplete: (File?) -> Unit
    ) {
        val (startDate, endDate) = filter.calculateRange()
        val filterDesc = buildFilterDescription(filter, typeFilter, categoryIds)

        launchSafely(onError = { error ->
            handleError(error)
            onComplete(null)
        }) {
            val transactions = transactionRepository.getTransactions(
                ledgerId = currentLedgerId,
                startDate = startDate,
                endDate = endDate,
                searchQuery = "",
                typeFilter = typeFilter,
                categoryIds = categoryIds
            ).first()

            val ledgerName = "Personal Ledger"
            val currencySymbol = _uiState.value.currencySymbol

            val result = PdfReportGenerator.generate(
                context = context,
                transactions = transactions,
                ledgerName = ledgerName,
                currencySymbol = currencySymbol,
                filterDescription = filterDesc
            )

            result.onSuccess { file ->
                onComplete(file)
            }.onFailure { err ->
                handleError(err.toUiError())
                onComplete(null)
            }
        }
    }

    private fun buildFilterDescription(
        filter: DateRangeFilter,
        typeFilter: String?,
        categoryIds: Set<String>
    ): String {
        val periodStr = when (filter) {
            is DateRangeFilter.AllTime -> "All Time"
            is DateRangeFilter.Today -> "Today"
            is DateRangeFilter.ThisWeek -> "This Week"
            is DateRangeFilter.ThisMonth -> "This Month"
            is DateRangeFilter.ThisYear -> "This Year"
            is DateRangeFilter.Custom -> "Custom Range"
        }
        val typeStr = when (typeFilter) {
            Constants.TransactionType.INCOME -> "Income only"
            Constants.TransactionType.EXPENSE -> "Expenses only"
            else -> "All Types"
        }
        val catStr = if (categoryIds.isEmpty()) "All Categories" else "${categoryIds.size} categories"
        return "$periodStr • $typeStr • $catStr"
    }

    // ---------- errors ----------

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun handleError(error: UiError) {
        _uiState.update { it.copy(error = error) }
    }
}
