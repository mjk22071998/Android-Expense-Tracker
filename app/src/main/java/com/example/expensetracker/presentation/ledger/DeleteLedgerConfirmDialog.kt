package com.example.expensetracker.presentation.ledger

import android.content.res.Configuration
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.Ledger
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun DeleteLedgerConfirmDialog(
    ledger: Ledger,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = extendedColors.cardBackground,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(extendedColors.crimsonExpense.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = null,
                    tint = extendedColors.crimsonExpense,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = "Delete Ledger?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = extendedColors.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Are you sure you want to delete \"${ledger.name}\"?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = extendedColors.textPrimary
                )

                Text(
                    text = "Existing transactions in this ledger will be preserved, and your active ledger will automatically revert to your Default ledger.",
                    style = MaterialTheme.typography.bodySmall,
                    color = extendedColors.textMuted
                )
            }
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
                Text(
                    text = "Delete Ledger",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Cancel",
                    color = extendedColors.textSecondary
                )
            }
        }
    )
}

@Preview(name = "Delete Ledger Dialog", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeleteLedgerConfirmDialogPreview() {
    ExpenseTrackerTheme {
        DeleteLedgerConfirmDialog(
            ledger = Ledger(
                id = "test",
                name = "Freelance",
                currencyCode = "USD",
                createdAt = 0L,
                updatedAt = 0L
            ),
            onDismiss = {},
            onConfirm = {}
        )
    }
}
