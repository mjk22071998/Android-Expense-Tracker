package com.example.expensetracker.presentation.transaction

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.expensetracker.Constants
import com.example.expensetracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeToggle(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        Constants.TransactionType.EXPENSE to "Expense",
        Constants.TransactionType.INCOME to "Income"
    )

    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (type, label) ->
            SegmentedButton(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (type == Constants.TransactionType.INCOME)
                                R.drawable.ic_trending_up
                            else
                                R.drawable.ic_trending_down
                        ),
                        contentDescription = null
                    )
                }
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}