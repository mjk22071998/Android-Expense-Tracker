package com.example.expensetracker.presentation.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun ClearAllTransactionsDialog(
    currencySymbol: String = "Rs",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = extendedColors.cardBackground,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = null,
                tint = extendedColors.crimsonExpense
            )
        },
        title = {
            Text(
                text = "Clear All Transactions?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = extendedColors.textPrimary
            )
        },
        text = {
            Text(
                text = "This will permanently remove all transaction records from your ledger and reset your balance to $currencySymbol 0.00. This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = extendedColors.textSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = extendedColors.crimsonExpense,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Clear All", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, extendedColors.cardBorder)
            ) {
                Text("Cancel", color = extendedColors.textSecondary)
            }
        }
    )
}

@Preview(name = "Clear All Dialog - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClearAllTransactionsDialogPreview() {
    ExpenseTrackerTheme {
        ClearAllTransactionsDialog(
            currencySymbol = "Rs",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
