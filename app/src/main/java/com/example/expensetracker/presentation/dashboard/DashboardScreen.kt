package com.example.expensetracker.presentation.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.ui.theme.ForestBackgroundDark
import com.example.expensetracker.ui.theme.ForestSurfaceDark
import com.example.expensetracker.ui.theme.ForestTextMutedDark
import com.example.expensetracker.ui.theme.ForestTextPrimaryDark

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

    // Back button closes search query if active
    BackHandler(enabled = uiState.isSearchActive || uiState.searchQuery.isNotEmpty()) {
        if (uiState.searchQuery.isNotEmpty()) {
            viewModel.onSearchQueryChanged("")
        }
        if (uiState.isSearchActive) {
            viewModel.onSearchClosed()
        }
    }

    // Show errors in snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error.message)
            viewModel.clearError()
        }
    }

    // Deep Atmospheric Organic Forest background brush
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            ForestBackgroundDark,
            ForestSurfaceDark,
            Color(0xFF07140D)
        ),
        startY = 0f,
        endY = 1600f
    )

    Scaffold(
        containerColor = Color.Transparent,
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Fixed hero section with Balance & Quick Actions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    // 1. Luxury Stitched Hero Balance Card with Dual Inflow/Outflow Pills
                    BalanceCard(
                        balance = uiState.balance,
                        currencySymbol = uiState.currencySymbol,
                        totalIncome = uiState.totalIncome,
                        totalExpense = uiState.totalExpense,
                        selectedTypeFilter = uiState.selectedTypeFilter,
                        onTypeFilterSelected = viewModel::onTypeFilterSelected
                    )

                    // 2. High-energy Quick Action Pills (Screen 1 Inspiration: Transfer / Target / Settings)
                    QuickActionsRow(
                        onAddTransaction = onAddTransaction,
                        onFilterCategories = { viewModel.onCategoryFilterSheetVisibilityChanged(true) },
                        onSettingsClick = onNavigateToSettings,
                        activeCategoryFilterCount = uiState.selectedCategoryIds.size
                    )

                    // 3. Integrated Search Pill Bar (Screen 1 Inspiration)
                    SearchPillBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged
                    )

                    // 4. Time Horizon Date Filters
                    FilterChipRow(
                        selectedFilter = uiState.selectedFilter,
                        onFilterSelected = viewModel::onFilterSelected
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section Header: Transaction History with record count pill badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForestTextPrimaryDark
                    )

                    Surface(
                        color = Color(0x221A3B29),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "${uiState.transactions.size} records",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestTextMutedDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Scrolling list of transactions with padding at bottom for FloatingBottomBar
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
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

            // Floating Pill Bottom Navigation Bar (Across all inspiration screens)
            FloatingBottomBar(
                currentDestination = BottomNavDestination.HOME,
                onHomeClick = { /* Already on Home */ },
                onLedgerClick = { /* Stay on active ledger */ },
                onCategoriesClick = { viewModel.onCategoryFilterSheetVisibilityChanged(true) },
                onSettingsClick = onNavigateToSettings,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
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
