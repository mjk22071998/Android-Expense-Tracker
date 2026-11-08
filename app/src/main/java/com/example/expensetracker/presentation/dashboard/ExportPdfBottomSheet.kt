package com.example.expensetracker.presentation.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.expensetracker.presentation.common.TooltipPosition
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.Constants
import com.example.expensetracker.R
import com.example.expensetracker.presentation.common.AppTooltip
import com.example.expensetracker.data.local.entity.Category
import com.example.expensetracker.domain.model.DateRangeFilter
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

enum class PdfExportAction {
    SAVE,
    SHARE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportPdfBottomSheet(
    initialFilter: DateRangeFilter,
    initialTypeFilter: String?,
    initialCategoryIds: Set<String>,
    allCategories: List<Category>,
    isGenerating: Boolean = false,
    onSaveToStorage: (filter: DateRangeFilter, typeFilter: String?, categoryIds: Set<String>) -> Unit,
    onShare: (filter: DateRangeFilter, typeFilter: String?, categoryIds: Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentFilter by remember { mutableStateOf(initialFilter) }
    var currentTypeFilter by remember { mutableStateOf(initialTypeFilter) }
    var currentCategoryIds by remember { mutableStateOf(initialCategoryIds) }

    var pendingAction by remember { mutableStateOf<PdfExportAction?>(null) }

    var showPeriodSheet by remember { mutableStateOf(false) }
    var showCustomDateDialog by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }

    val periodLabel = when (currentFilter) {
        is DateRangeFilter.AllTime -> "All Time"
        is DateRangeFilter.Today -> "Today"
        is DateRangeFilter.ThisWeek -> "This Week"
        is DateRangeFilter.ThisMonth -> "This Month"
        is DateRangeFilter.ThisYear -> "This Year"
        is DateRangeFilter.Custom -> "Custom Range"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = extendedColors.cardBackground,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(extendedColors.cardBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Export to PDF",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary
                    )
                    Text(
                        text = "Save statement to device storage or share via apps",
                        style = MaterialTheme.typography.bodySmall,
                        color = extendedColors.textSecondary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = extendedColors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Section 1: Period Selection (Inherited & Modifiable)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Time Period",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = extendedColors.textSecondary
                )

                Surface(
                    onClick = { showPeriodSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = extendedColors.cardElevated,
                    border = BorderStroke(1.dp, extendedColors.cardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_date_range),
                                contentDescription = null,
                                tint = ElectricLime,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = periodLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = extendedColors.textPrimary
                            )
                        }

                        Text(
                            text = "Change",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricLime
                        )
                    }
                }
            }

            // Section 2: Transaction Type Filter (All / Income / Expense)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Transaction Type",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = extendedColors.textSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TypeFilterPill(
                        label = "All Types",
                        isSelected = currentTypeFilter == null,
                        onClick = { currentTypeFilter = null },
                        modifier = Modifier.weight(1f)
                    )
                    TypeFilterPill(
                        label = "Income",
                        isSelected = currentTypeFilter == Constants.TransactionType.INCOME,
                        onClick = { currentTypeFilter = Constants.TransactionType.INCOME },
                        modifier = Modifier.weight(1f)
                    )
                    TypeFilterPill(
                        label = "Expense",
                        isSelected = currentTypeFilter == Constants.TransactionType.EXPENSE,
                        onClick = { currentTypeFilter = Constants.TransactionType.EXPENSE },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section 3: Category Filter
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = extendedColors.textSecondary
                )

                Surface(
                    onClick = { showCategorySheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = extendedColors.cardElevated,
                    border = BorderStroke(1.dp, extendedColors.cardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_category),
                                contentDescription = null,
                                tint = extendedColors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (currentCategoryIds.isEmpty()) "All Categories"
                                else "${currentCategoryIds.size} categories filtered",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = extendedColors.textPrimary
                            )
                        }

                        Text(
                            text = if (currentCategoryIds.isEmpty()) "Filter" else "Edit",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricLime
                        )
                    }
                }
            }

            // Info Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0x11FFFFFF),
                border = BorderStroke(1.dp, Color(0x22FFFFFF))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_picture_as_pdf),
                        contentDescription = null,
                        tint = ElectricLime,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Standard ISO A4 statement report with executive totals and itemized transaction table.",
                        style = MaterialTheme.typography.bodySmall,
                        color = extendedColors.textSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Action Buttons: Save to Phone Storage + Share PDF
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary Option: Save in Phone / SD Storage
                AppTooltip(
                    text = "Save statement to device storage or SD card Downloads",
                    position = TooltipPosition.Above,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            pendingAction = PdfExportAction.SAVE
                            onSaveToStorage(currentFilter, currentTypeFilter, currentCategoryIds)
                        },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricLime,
                            contentColor = ElectricLimeOn
                        )
                    ) {
                        if (isGenerating && pendingAction == PdfExportAction.SAVE) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = ElectricLimeOn,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Saving to Device...",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_download),
                                contentDescription = null,
                                tint = ElectricLimeOn,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save in Phone Storage",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                // Secondary Option: Share PDF
                AppTooltip(
                    text = "Share PDF statement via apps (WhatsApp, Drive, Email)",
                    position = TooltipPosition.Above,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            pendingAction = PdfExportAction.SHARE
                            onShare(currentFilter, currentTypeFilter, currentCategoryIds)
                        },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = extendedColors.cardElevated,
                            contentColor = extendedColors.textPrimary
                        ),
                        border = BorderStroke(1.dp, extendedColors.cardBorder)
                    ) {
                        if (isGenerating && pendingAction == PdfExportAction.SHARE) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = extendedColors.textPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Preparing PDF...",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_share),
                                contentDescription = null,
                                tint = extendedColors.textPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Share PDF",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Modal: Period Selection inside Export
    if (showPeriodSheet) {
        PeriodFilterBottomSheet(
            selectedFilter = currentFilter,
            onFilterSelected = { filter ->
                currentFilter = filter
                showPeriodSheet = false
            },
            onCustomRangeClick = {
                showPeriodSheet = false
                showCustomDateDialog = true
            },
            onDismiss = { showPeriodSheet = false }
        )
    }

    // Modal: Custom Date Picker inside Export
    if (showCustomDateDialog) {
        CustomDateRangePickerDialog(
            onDismiss = { showCustomDateDialog = false },
            onRangeSelected = { start, end ->
                currentFilter = DateRangeFilter.Custom(start, end)
                showCustomDateDialog = false
            }
        )
    }

    // Modal: Category Filter inside Export
    if (showCategorySheet) {
        CategoryFilterBottomSheet(
            allCategories = allCategories,
            selectedCategoryIds = currentCategoryIds,
            onCategoryToggled = { catId ->
                currentCategoryIds = if (currentCategoryIds.contains(catId)) {
                    currentCategoryIds - catId
                } else {
                    currentCategoryIds + catId
                }
            },
            onClear = {
                currentCategoryIds = emptySet()
            },
            onApply = {
                showCategorySheet = false
            },
            onDismiss = { showCategorySheet = false }
        )
    }
}

@Composable
private fun TypeFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) ElectricLime else extendedColors.cardElevated,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) ElectricLime else extendedColors.cardBorder
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) ElectricLimeOn else extendedColors.textPrimary,
                fontSize = 13.sp
            )
        }
    }
}

@Preview(name = "Export PDF Bottom Sheet - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ExportPdfBottomSheetPreview() {
    ExpenseTrackerTheme {
        ExportPdfBottomSheet(
            initialFilter = DateRangeFilter.ThisMonth,
            initialTypeFilter = null,
            initialCategoryIds = emptySet(),
            allCategories = emptyList(),
            onSaveToStorage = { _, _, _ -> },
            onShare = { _, _, _ -> },
            onDismiss = {}
        )
    }
}
