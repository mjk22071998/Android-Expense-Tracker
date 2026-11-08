package com.example.expensetracker.presentation.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.domain.model.DateRangeFilter
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodFilterBottomSheet(
    selectedFilter: DateRangeFilter,
    onFilterSelected: (DateRangeFilter) -> Unit,
    onCustomRangeClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val periodOptions = listOf(
        PeriodOption(
            filter = DateRangeFilter.AllTime,
            title = "All Time",
            subtitle = "Show all transactions from entire history",
            iconRes = R.drawable.ic_receipt_long
        ),
        PeriodOption(
            filter = DateRangeFilter.ThisMonth,
            title = "This Month",
            subtitle = "Current calendar month",
            iconRes = R.drawable.ic_date_range
        ),
        PeriodOption(
            filter = DateRangeFilter.ThisWeek,
            title = "This Week",
            subtitle = "Current calendar week (Mon – Sun)",
            iconRes = R.drawable.ic_date_range
        ),
        PeriodOption(
            filter = DateRangeFilter.Today,
            title = "Today",
            subtitle = "Transactions logged today",
            iconRes = R.drawable.ic_date_range
        ),
        PeriodOption(
            filter = DateRangeFilter.ThisYear,
            title = "This Year",
            subtitle = "Current calendar year",
            iconRes = R.drawable.ic_date_range
        )
    )

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
                .padding(horizontal = 24.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Title, subtitle and close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Select Time Period",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary
                    )
                    Text(
                        text = "Choose which timeframe to calculate and display",
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

            Spacer(modifier = Modifier.height(6.dp))

            // Standard period options
            periodOptions.forEach { option ->
                val isSelected = selectedFilter == option.filter
                PeriodOptionItem(
                    title = option.title,
                    subtitle = option.subtitle,
                    iconRes = option.iconRes,
                    isSelected = isSelected,
                    onClick = { onFilterSelected(option.filter) }
                )
            }

            // Custom Date Range Option
            val isCustomSelected = selectedFilter is DateRangeFilter.Custom
            PeriodOptionItem(
                title = if (isCustomSelected) "Custom Range (Active)" else "Custom Date Range...",
                subtitle = if (isCustomSelected) "Tap to change the start and end dates" else "Pick custom start and end dates from calendar",
                iconRes = R.drawable.ic_date_range,
                isSelected = isCustomSelected,
                onClick = onCustomRangeClick
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

private data class PeriodOption(
    val filter: DateRangeFilter,
    val title: String,
    val subtitle: String,
    val iconRes: Int
)

@Composable
private fun PeriodOptionItem(
    title: String,
    subtitle: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0x22D4F63D) else extendedColors.cardElevated,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) ElectricLime else extendedColors.cardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) ElectricLime else extendedColors.cardBorder),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = if (isSelected) ElectricLimeOn else extendedColors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Title & Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) ElectricLime else extendedColors.textPrimary,
                    fontSize = 15.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = extendedColors.textSecondary,
                    fontSize = 12.sp
                )
            }

            // Checkmark indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(ElectricLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "Selected",
                        tint = ElectricLimeOn,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = extendedColors.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview(name = "Period Filter Bottom Sheet - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PeriodFilterBottomSheetPreview() {
    ExpenseTrackerTheme {
        PeriodFilterBottomSheet(
            selectedFilter = DateRangeFilter.AllTime,
            onFilterSelected = {},
            onCustomRangeClick = {},
            onDismiss = {}
        )
    }
}
