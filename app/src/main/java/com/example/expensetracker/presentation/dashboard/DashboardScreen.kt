package com.example.expensetracker.presentation.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun DashboardScreen(
    onAddTransaction: () -> Unit,
    onEditTransaction: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTransaction by remember { mutableStateOf<TransactionWithCategory?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val extendedColors = ExpenseTrackerTheme.extendedColors

    // back button closes search instead of exiting the app
    BackHandler(enabled = uiState.isSearchActive) {
        viewModel.onSearchClosed()
    }

    // show errors in a snackbar, then clear them
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error.message)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DashboardTopBar(
                isSearchActive = uiState.isSearchActive,
                searchQuery = uiState.searchQuery,
                activeCategoryFilterCount = uiState.selectedCategoryIds.size,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onSearchActivated = viewModel::onSearchActivated,
                onSearchClosed = viewModel::onSearchClosed,
                onFilterClick = { viewModel.onCategoryFilterSheetVisibilityChanged(true) },
                onSettingsClick = onNavigateToSettings
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = ElectricLime,
                contentColor = ElectricLimeOn,
                shape = CircleShape,
                modifier = Modifier.size(58.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Transaction",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Fixed hero & filters section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                BalanceCard(
                    balance = uiState.balance,
                    currencySymbol = uiState.currencySymbol
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Income",
                        amount = uiState.totalIncome,
                        currencySymbol = uiState.currencySymbol,
                        isIncome = true
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Expense",
                        amount = uiState.totalExpense,
                        currencySymbol = uiState.currencySymbol,
                        isIncome = false
                    )
                }

                FilterChipRow(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = viewModel::onFilterSelected
                )

                TypeFilterRow(
                    selectedType = uiState.selectedTypeFilter,
                    onTypeSelected = viewModel::onTypeFilterSelected
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Transaction History Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary
                )

                Text(
                    text = "${uiState.transactions.size} records",
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textMuted
                )
            }

            // Scrolling list of transactions
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 88.dp)
            ) {
                if (uiState.transactions.isEmpty()) {
                    item { EmptyTransactionState() }
                } else {
                    items(
                        items = uiState.transactions,
                        key = { it.id }
                    ) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            currencySymbol = uiState.currencySymbol,
                            onClick = { selectedTransaction = transaction },
                            onEdit = { onEditTransaction(transaction.id) },
                            onDelete = { viewModel.softDeleteTransaction(transaction) }
                        )
                    }
                }
            }
        }
    }

    selectedTransaction?.let { transaction ->
        TransactionDetailDialog(
            transaction = transaction,
            currencySymbol = uiState.currencySymbol,
            onDismiss = { selectedTransaction = null },
            onEdit = {
                selectedTransaction = null
                onEditTransaction(transaction.id)
            }
        )
    }

    if (uiState.isCategoryFilterSheetVisible) {
        CategoryFilterBottomSheet(
            allCategories = uiState.allCategories,
            selectedCategoryIds = uiState.selectedCategoryIds,
            onCategoryToggled = viewModel::onCategoryFilterToggled,
            onClear = viewModel::onClearCategoryFilters,
            onApply = viewModel::onApplyCategoryFilters,
            onDismiss = { viewModel.onCategoryFilterSheetVisibilityChanged(false) }
        )
    }
}
