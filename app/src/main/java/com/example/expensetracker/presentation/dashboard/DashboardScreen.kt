package com.example.expensetracker.presentation.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.domain.model.CurrencyHelper.formatAmount
import com.example.expensetracker.domain.model.DateRangeFilter
import com.example.expensetracker.ui.theme.ElectricLime

@Composable
fun DashboardScreen(
    onAddTransaction: () -> Unit,
    onEditTransaction: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTransaction by remember { mutableStateOf<TransactionWithCategory?>(null) }
    var showTotalCardsSheet by remember { mutableStateOf(false) }
    var showPeriodPicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Back button closes active search query
    BackHandler(enabled = uiState.searchQuery.isNotEmpty()) {
        viewModel.onSearchQueryChanged("")
    }

    // Show errors in snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error.message)
            viewModel.clearError()
        }
    }

    // Luminous Moss/Canopy Green vertical gradient from reference inspiration
    val backgroundBrush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF2E633B), // Radiant moss green at top
            0.16f to Color(0xFF1E482A), // Rich forest canopy
            0.36f to Color(0xFF122E1B), // Dark green transition
            0.62f to Color(0xFF0C1F13), // Obsidian forest
            1.0f to Color(0xFF06110A)  // Deep base
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DashboardTopBar(
                title = "Savings Account",
                activeCategoryFilterCount = uiState.selectedCategoryIds.size,
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
            // Ambient luminous green glow at top right matching reference image
            Box(
                modifier = Modifier
                    .size(380.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 120.dp, y = (-100).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x66489B5F),
                                Color(0x33357547),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // ============================================================
                // 1. Hero Balance (Open directly on canvas, NO BOX/CARD)
                // ============================================================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Savings Account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFA5C4B2),
                        letterSpacing = 0.4.sp
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = uiState.currencySymbol,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = formatAmount(uiState.balance),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-1.5).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "✦",
                            color = ElectricLime,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (uiState.balance >= 0) "+$ 1632 Than Last Month"
                                   else "Expenses exceeding income — review budget",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA5C4B2)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ============================================================
                // 2. High-energy Action Pills Row (Transfer / Target / Settings)
                // ============================================================
                val periodLabel = when (uiState.selectedFilter) {
                    is DateRangeFilter.Today -> "Today"
                    is DateRangeFilter.ThisWeek -> "This Week"
                    is DateRangeFilter.ThisMonth -> "This Month"
                    is DateRangeFilter.ThisYear -> "This Year"
                    is DateRangeFilter.Custom -> "Custom"
                }

                QuickActionsRow(
                    onAddTransaction = onAddTransaction,
                    onFilterPeriodClick = { showPeriodPicker = true },
                    onSettingsClick = onNavigateToSettings,
                    periodLabel = periodLabel
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ============================================================
                // 3. Integrated Search Pill Bar
                // ============================================================
                SearchPillBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged
                )

                Spacer(modifier = Modifier.height(22.dp))

                // ============================================================
                // 4. Transaction History Header (with "See Details")
                // ============================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.2).sp
                    )

                    Text(
                        text = "See Details",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFA5C4B2),
                        modifier = Modifier
                            .clickable { showTotalCardsSheet = true }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ============================================================
                // 5. Clean Seamless Transaction Rows
                // ============================================================
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
                ) {
                    if (uiState.transactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No transactions found for this period",
                                    color = Color(0xFFA5C4B2),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
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

            // ============================================================
            // 6. Floating Pill Bottom Navigation Bar (Across All Screens)
            // ============================================================
            FloatingBottomBar(
                currentDestination = BottomNavDestination.HOME,
                onHomeClick = { /* Already on Home */ },
                onLedgerClick = { showTotalCardsSheet = true },
                onCategoriesClick = { viewModel.onCategoryFilterSheetVisibilityChanged(true) },
                onSettingsClick = onNavigateToSettings,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }

    // Modal: Screen 2 Total Cards & Stitched Leathercraft Sheet
    if (showTotalCardsSheet) {
        TotalCardsBottomSheet(
            balance = uiState.balance,
            totalIncome = uiState.totalIncome,
            totalExpense = uiState.totalExpense,
            currencySymbol = uiState.currencySymbol,
            onDismiss = { showTotalCardsSheet = false }
        )
    }

    // Modal: Period Date Range Selection
    if (showPeriodPicker) {
        CustomDateRangePickerDialog(
            onDismiss = { showPeriodPicker = false },
            onRangeSelected = { start, end ->
                viewModel.onFilterSelected(DateRangeFilter.Custom(start, end))
                showPeriodPicker = false
            }
        )
    }

    // Dialog: Transaction Receipt Detail
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

    // BottomSheet: Category Filter
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
