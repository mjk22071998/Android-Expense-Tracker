package com.example.expensetracker.presentation.dashboard

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.domain.model.CurrencyHelper.formatAmount
import com.example.expensetracker.domain.model.DateRangeFilter
import com.example.expensetracker.domain.usecase.PdfStorageHelper
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.ui.theme.ExtendedColors
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    onAddTransaction: () -> Unit,
    onEditTransaction: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 5-Tab non-slidable navigation state
    var currentTab by rememberSaveable { mutableStateOf(BottomNavDestination.HOME) }

    var selectedTransaction by remember { mutableStateOf<TransactionWithCategory?>(null) }
    var showPeriodSheet by remember { mutableStateOf(false) }
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var pendingSaveFile by remember { mutableStateOf<File?>(null) }

    // Fallback or explicit Document creation launcher (Storage Access Framework)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { destinationUri ->
        val file = pendingSaveFile
        if (destinationUri != null && file != null) {
            try {
                context.contentResolver.openOutputStream(destinationUri)?.use { outStream ->
                    file.inputStream().use { inStream ->
                        inStream.copyTo(outStream)
                    }
                }
                Toast.makeText(context, "Report saved successfully", Toast.LENGTH_SHORT).show()
                coroutineScope.launch {
                    val res = snackbarHostState.showSnackbar(
                        message = "Report saved successfully",
                        actionLabel = "Open",
                        duration = SnackbarDuration.Long
                    )
                    if (res == SnackbarResult.ActionPerformed) {
                        PdfStorageHelper.openPdf(context, destinationUri)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                pendingSaveFile = null
            }
        } else {
            pendingSaveFile = null
        }
    }

    // Permission launcher for Android 9 and below (API <= 28)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val file = pendingSaveFile
        if (file != null) {
            if (isGranted) {
                val saveResult = PdfStorageHelper.saveReportToStorage(context, file)
                saveResult.onSuccess { result ->
                    Toast.makeText(context, "Saved to ${result.displayPath}", Toast.LENGTH_LONG).show()
                    coroutineScope.launch {
                        val snackbarRes = snackbarHostState.showSnackbar(
                            message = "Saved to ${result.displayPath}",
                            actionLabel = "Open",
                            duration = SnackbarDuration.Long
                        )
                        if (snackbarRes == SnackbarResult.ActionPerformed) {
                            PdfStorageHelper.openPdf(context, result.uri)
                        }
                    }
                    pendingSaveFile = null
                }.onFailure {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    createDocumentLauncher.launch("Expense_Report_$timeStamp.pdf")
                }
            } else {
                Toast.makeText(context, "Storage permission denied. Choose location to save:", Toast.LENGTH_SHORT).show()
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                createDocumentLauncher.launch("Expense_Report_$timeStamp.pdf")
            }
        }
    }

    // Back handling: If on secondary tab, return to Home. If on Home with search open, clear search.
    BackHandler(enabled = currentTab != BottomNavDestination.HOME) {
        currentTab = BottomNavDestination.HOME
    }

    BackHandler(enabled = currentTab == BottomNavDestination.HOME && uiState.searchQuery.isNotEmpty()) {
        viewModel.onSearchQueryChanged("")
    }

    // Show errors in snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error.message)
            viewModel.clearError()
        }
    }

    // Luxury Organic Forest vertical gradient with full light/dark responsiveness
    val backgroundBrush = if (extendedColors.isDark) {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFF2E633B), // Radiant moss green at top
                0.16f to Color(0xFF1E482A), // Rich forest canopy
                0.36f to Color(0xFF122E1B), // Dark green transition
                0.62f to Color(0xFF0C1F13), // Obsidian forest
                1.0f to Color(0xFF06110A)   // Deep base
            )
        )
    } else {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFFDCE8E0), // Soft luminous sage tint at top
                0.18f to Color(0xFFE9F1EC), // Subtle mint transition
                0.45f to Color(0xFFF2F7F4), // Clean surface (ForestBackgroundLight)
                1.0f to Color(0xFFF7FAF8)   // Base background
            )
        )
    }

    val periodLabel = when (uiState.selectedFilter) {
        is DateRangeFilter.AllTime -> "All Time"
        is DateRangeFilter.Today -> "Today"
        is DateRangeFilter.ThisWeek -> "This Week"
        is DateRangeFilter.ThisMonth -> "This Month"
        is DateRangeFilter.ThisYear -> "This Year"
        is DateRangeFilter.Custom -> "Custom"
    }

    val topBarTitle = when (currentTab) {
        BottomNavDestination.HOME -> "Personal Expenses"
        BottomNavDestination.ANALYTICS -> "Visual Analytics"
        BottomNavDestination.LEDGER -> "Financial Overview"
        BottomNavDestination.CATEGORIES -> "Categories"
        BottomNavDestination.SETTINGS -> "Settings"
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DashboardTopBar(
                title = topBarTitle,
                onExportClick = { showExportSheet = true }
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
                            colors = if (extendedColors.isDark) {
                                listOf(
                                    Color(0x66489B5F),
                                    Color(0x33357547),
                                    Color.Transparent
                                )
                            } else {
                                listOf(
                                    Color(0x33A3D9B5),
                                    Color(0x1A85C49A),
                                    Color.Transparent
                                )
                            }
                        ),
                        shape = CircleShape
                    )
            )

            // Tab Content Switcher (Strictly tap-based, NOT slidable)
            when (currentTab) {
                BottomNavDestination.HOME -> {
                    HomeTabContent(
                        balance = uiState.balance,
                        currencySymbol = uiState.currencySymbol,
                        periodLabel = periodLabel,
                        searchQuery = uiState.searchQuery,
                        transactions = uiState.transactions,
                        extendedColors = extendedColors,
                        onAddTransaction = onAddTransaction,
                        onPeriodClick = { showPeriodSheet = true },
                        onSearchQueryChange = viewModel::onSearchQueryChanged,
                        onTransactionClick = { selectedTransaction = it },
                        onEditTransaction = onEditTransaction,
                        onDeleteTransaction = { viewModel.softDeleteTransaction(it) }
                    )
                }

                BottomNavDestination.ANALYTICS -> {
                    AnalyticsTabContent(
                        chartDataSet = uiState.chartDataSet,
                        selectedChartMode = uiState.selectedChartMode,
                        currencySymbol = uiState.currencySymbol,
                        periodLabel = periodLabel,
                        transactions = uiState.transactions,
                        onChartModeSelected = viewModel::onChartModeSelected,
                        onPeriodClick = { showPeriodSheet = true }
                    )
                }

                BottomNavDestination.LEDGER -> {
                    LedgerTabContent(
                        selectedFilter = uiState.selectedFilter,
                        transactions = uiState.transactions,
                        allTimeBalance = uiState.balance,
                        monthlyTrendDelta = uiState.monthlyTrendDelta,
                        currencySymbol = uiState.currencySymbol,
                        onPeriodClick = { showPeriodSheet = true }
                    )
                }

                BottomNavDestination.CATEGORIES -> {
                    CategoriesTabContent(
                        allCategories = uiState.allCategories,
                        selectedCategoryIds = uiState.selectedCategoryIds,
                        categoryUsageMap = uiState.categoryUsageMap,
                        currencySymbol = uiState.currencySymbol,
                        onCategoryToggled = viewModel::onCategoryFilterToggled,
                        onClearFilters = viewModel::onClearCategoryFilters,
                        onApplyFilters = viewModel::onApplyCategoryFilters
                    )
                }

                BottomNavDestination.SETTINGS -> {
                    SettingsTabContent(
                        onExportClick = { showExportSheet = true }
                    )
                }
            }

            // Floating Pill Bottom Navigation Bar (Across All Tabs, non-slidable)
            FloatingBottomBar(
                currentDestination = currentTab,
                onHomeClick = { currentTab = BottomNavDestination.HOME },
                onAnalyticsClick = { currentTab = BottomNavDestination.ANALYTICS },
                onLedgerClick = { currentTab = BottomNavDestination.LEDGER },
                onCategoriesClick = { currentTab = BottomNavDestination.CATEGORIES },
                onSettingsClick = { currentTab = BottomNavDestination.SETTINGS },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }

    // Modal: Period Filter Selection Bottom Sheet
    if (showPeriodSheet) {
        PeriodFilterBottomSheet(
            selectedFilter = uiState.selectedFilter,
            onFilterSelected = { filter ->
                viewModel.onFilterSelected(filter)
                showPeriodSheet = false
            },
            onCustomRangeClick = {
                showPeriodSheet = false
                showCustomDatePicker = true
            },
            onDismiss = { showPeriodSheet = false }
        )
    }

    // Modal: Custom Date Range Picker
    if (showCustomDatePicker) {
        CustomDateRangePickerDialog(
            onDismiss = { showCustomDatePicker = false },
            onRangeSelected = { start, end ->
                viewModel.onFilterSelected(DateRangeFilter.Custom(start, end))
                showCustomDatePicker = false
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

    // BottomSheet: Export to PDF Modal
    if (showExportSheet) {
        ExportPdfBottomSheet(
            initialFilter = uiState.selectedFilter,
            initialTypeFilter = uiState.selectedTypeFilter,
            initialCategoryIds = uiState.selectedCategoryIds,
            allCategories = uiState.allCategories,
            isGenerating = isGeneratingPdf,
            onSaveToStorage = { filter, typeFilter, categoryIds ->
                isGeneratingPdf = true
                viewModel.generatePdfReport(
                    filter = filter,
                    typeFilter = typeFilter,
                    categoryIds = categoryIds
                ) { file ->
                    isGeneratingPdf = false
                    showExportSheet = false
                    if (file != null) {
                        pendingSaveFile = file
                        // Check permissions first!
                        if (PdfStorageHelper.hasStoragePermission(context)) {
                            val saveResult = PdfStorageHelper.saveReportToStorage(context, file)
                            saveResult.onSuccess { result ->
                                Toast.makeText(context, "Saved to ${result.displayPath}", Toast.LENGTH_LONG).show()
                                coroutineScope.launch {
                                    val snackbarRes = snackbarHostState.showSnackbar(
                                        message = "Saved to ${result.displayPath}",
                                        actionLabel = "Open",
                                        duration = SnackbarDuration.Long
                                    )
                                    if (snackbarRes == SnackbarResult.ActionPerformed) {
                                        PdfStorageHelper.openPdf(context, result.uri)
                                    }
                                }
                                pendingSaveFile = null
                            }.onFailure {
                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                createDocumentLauncher.launch("Expense_Report_$timeStamp.pdf")
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }
                }
            },
            onShare = { filter, typeFilter, categoryIds ->
                isGeneratingPdf = true
                viewModel.generatePdfReport(
                    filter = filter,
                    typeFilter = typeFilter,
                    categoryIds = categoryIds
                ) { file ->
                    isGeneratingPdf = false
                    showExportSheet = false
                    if (file != null) {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, "Expense Report - Personal Ledger")
                            clipData = ClipData.newRawUri("Expense Report", uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        val chooserIntent = Intent.createChooser(shareIntent, "Share Expense Report (PDF)").apply {
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(chooserIntent)
                    }
                }
            },
            onDismiss = {
                if (!isGeneratingPdf) showExportSheet = false
            }
        )
    }
}

@Composable
private fun HomeTabContent(
    balance: Double,
    currencySymbol: String,
    periodLabel: String,
    searchQuery: String,
    transactions: List<TransactionWithCategory>,
    extendedColors: ExtendedColors,
    onAddTransaction: () -> Unit,
    onPeriodClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onTransactionClick: (TransactionWithCategory) -> Unit,
    onEditTransaction: (String) -> Unit,
    onDeleteTransaction: (TransactionWithCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Hero Balance (Open directly on canvas, NO BOX/CARD)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary,
                letterSpacing = 0.4.sp
            )

            val formattedBalance = if (balance < 0) {
                "− $currencySymbol ${formatAmount(kotlin.math.abs(balance))}"
            } else {
                "$currencySymbol ${formatAmount(balance)}"
            }

            Text(
                text = formattedBalance,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = if (extendedColors.isDark) Color.White else extendedColors.textPrimary,
                letterSpacing = (-1.0).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // High-energy Action Pills Row
        QuickActionsRow(
            onAddTransaction = onAddTransaction,
            onFilterPeriodClick = onPeriodClick,
            periodLabel = periodLabel
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Integrated Search Pill Bar
        SearchPillBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Full Transaction History LazyColumn (uncluttered, dedicated view)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 2.dp, bottom = 100.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (extendedColors.isDark) Color.White else extendedColors.textPrimary,
                        letterSpacing = (-0.2).sp
                    )

                    Text(
                        text = "${transactions.size} records",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary
                    )
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions found for this period",
                            color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(
                    items = transactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        currencySymbol = currencySymbol,
                        onClick = { onTransactionClick(transaction) },
                        onEdit = { onEditTransaction(transaction.id) },
                        onDelete = { onDeleteTransaction(transaction) }
                    )
                }
            }
        }
    }
}
