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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangePickerDialog(
    onDismiss: () -> Unit,
    onRangeSelected: (startDate: Long, endDate: Long) -> Unit
) {
    val todayEndOfDay = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    val rangeState = rememberDateRangePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= todayEndOfDay
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
                        onRangeSelected(start, end)
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