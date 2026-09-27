package com.example.expensetracker.presentation.dashboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.domain.model.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangePickerDialog(
    onDismiss: () -> Unit,
    onRangeSelected: (startDate: Long, endDate: Long) -> Unit
) {
    val todayUtcMillis = remember { DateUtils.todayAsUtcMidnight() }

    val rangeState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = todayUtcMillis,
        initialSelectedEndDateMillis = todayUtcMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= todayUtcMillis
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = rangeState.selectedStartDateMillis
                    val end = rangeState.selectedEndDateMillis
                    if (start != null && end != null) {
                        onRangeSelected(
                            DateUtils.utcMidnightToLocalStartOfDay(start),
                            DateUtils.utcMidnightToLocalEndOfDay(end)
                        )
                    }
                },
                enabled = rangeState.selectedStartDateMillis != null &&
                        rangeState.selectedEndDateMillis != null
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = rangeState,
            modifier = Modifier.fillMaxWidth(),
            title = {
                Text(
                    text = "Select date range",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        )
    }
}