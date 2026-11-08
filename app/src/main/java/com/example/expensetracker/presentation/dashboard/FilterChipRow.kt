package com.example.expensetracker.presentation.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.domain.model.DateRangeFilter
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun FilterChipRow(
    selectedFilter: DateRangeFilter,
    onFilterSelected: (DateRangeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDateRangePicker by remember { mutableStateOf(false) }

    val filters = listOf(
        DateRangeFilter.Today to "Today",
        DateRangeFilter.ThisWeek to "This Week",
        DateRangeFilter.ThisMonth to "This Month",
        DateRangeFilter.ThisYear to "This Year"
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(filters) { (filter, label) ->
            val isSelected = selectedFilter == filter
            StyledFilterChip(
                label = label,
                isSelected = isSelected,
                onClick = { onFilterSelected(filter) }
            )
        }

        item {
            val isCustomSelected = selectedFilter is DateRangeFilter.Custom
            StyledFilterChip(
                label = "Custom",
                isSelected = isCustomSelected,
                leadingIconRes = if (isCustomSelected) R.drawable.ic_check else R.drawable.ic_settings,
                onClick = { showDateRangePicker = true }
            )
        }
    }

    if (showDateRangePicker) {
        CustomDateRangePickerDialog(
            onDismiss = { showDateRangePicker = false },
            onRangeSelected = { start, end ->
                onFilterSelected(DateRangeFilter.Custom(start, end))
                showDateRangePicker = false
            }
        )
    }
}

@Composable
fun StyledFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIconRes: Int? = if (isSelected) R.drawable.ic_check else null
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        shape = CircleShape,
        modifier = modifier,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        },
        leadingIcon = leadingIconRes?.let { iconId ->
            {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        },
        border = BorderStroke(
            1.dp,
            if (isSelected) ElectricLime else extendedColors.cardBorder
        ),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ElectricLime,
            selectedLabelColor = ElectricLimeOn,
            selectedLeadingIconColor = ElectricLimeOn,
            containerColor = extendedColors.cardBackground,
            labelColor = extendedColors.textSecondary,
            iconColor = extendedColors.textMuted
        )
    )
}

@Preview(name = "Filter Chips - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FilterChipRowPreview() {
    ExpenseTrackerTheme {
        FilterChipRow(
            selectedFilter = DateRangeFilter.ThisMonth,
            onFilterSelected = {}
        )
    }
}
