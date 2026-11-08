package com.example.expensetracker.presentation.dashboard

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.expensetracker.ui.theme.ElectricLime
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTransaction by remember { mutableStateOf<TransactionWithCategory?>(null) }
    var showTotalCardsSheet by remember { mutableStateOf(false) }
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
                title = "Personal Expenses",
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
                        text = "Total Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFA5C4B2),
                        letterSpacing = 0.4.sp
                    )

                    val formattedBalance = if (uiState.balance < 0) {
                        "− ${uiState.currencySymbol} ${formatAmount(kotlin.math.abs(uiState.balance))}"
                    } else {
                        "${uiState.currencySymbol} ${formatAmount(uiState.balance)}"
                    }

                    Text(
                        text = formattedBalance,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-1.0).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                }

                Spacer(modifier = Modifier.height(20.dp))

                // ============================================================
                // 2. High-energy Action Pills Row (Transfer / Target / Settings)
                // ============================================================
                val periodLabel = when (uiState.selectedFilter) {
                    is DateRangeFilter.AllTime -> "All Time"
                    is DateRangeFilter.Today -> "Today"
                    is DateRangeFilter.ThisWeek -> "This Week"
                    is DateRangeFilter.ThisMonth -> "This Month"
                    is DateRangeFilter.ThisYear -> "This Year"
                    is DateRangeFilter.Custom -> "Custom"
                }

                QuickActionsRow(
                    onAddTransaction = onAddTransaction,
                    onFilterPeriodClick = { showPeriodSheet = true },
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
                // 4. Transaction History Header
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
                        text = "${uiState.transactions.size} records",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFA5C4B2)
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
