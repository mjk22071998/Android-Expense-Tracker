package com.example.expensetracker.presentation.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.expensetracker.Constants
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.domain.model.CurrencyHelper.formatAmount
import com.example.expensetracker.domain.model.getCategoryIcon
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun TransactionDetailDialog(
    transaction: TransactionWithCategory,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    val isIncome = transaction.type == Constants.TransactionType.INCOME
    val accentColor = if (isIncome) extendedColors.emeraldIncome else extendedColors.crimsonExpense
    val iconBg = if (isIncome) Color(0x2210B981) else Color(0x22F43F5E)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = extendedColors.cardBackground,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category Avatar
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = getCategoryIcon(transaction.categoryIcon)),
                        contentDescription = transaction.categoryName,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Amount
                Text(
                    text = "${if (isIncome) "+" else "-"} $currencySymbol ${formatAmount(transaction.amount)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    letterSpacing = (-0.5).sp
                )

                // Type Badge
                Surface(
                    color = if (isIncome) Color(0x1810B981) else Color(0x18F43F5E),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isIncome) "Income Deposit" else "Expense Outflow",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizontalDivider(color = extendedColors.cardBorder, thickness = 1.dp)

                DetailRow(
                    label = "Category",
                    value = transaction.categoryName,
                    valueColor = extendedColors.textPrimary
                )

                DetailRow(
                    label = "Date & Time",
                    value = formatTransactionDate(transaction.date),
                    valueColor = extendedColors.textPrimary
                )

                if (transaction.note.isNotBlank()) {
                    DetailRow(
                        label = "Note",
                        value = transaction.note,
                        valueColor = extendedColors.textPrimary
                    )
                }

                DetailRow(
                    label = "Ledger",
                    value = "Personal Account",
                    valueColor = extendedColors.textSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricLime,
                    contentColor = ElectricLimeOn
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Edit Transaction",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.cardBorder)
            ) {
                Text(
                    text = "Close",
                    color = extendedColors.textSecondary
                )
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = extendedColors.textMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Preview(name = "Detail Dialog - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TransactionDetailDialogPreview() {
    ExpenseTrackerTheme {
        TransactionDetailDialog(
            transaction = TransactionWithCategory(
                id = "1",
                ledgerId = "ledger_default",
                categoryId = "cat_food",
                categoryIcon = "restaurant",
                categoryName = "Food & Dining",
                amount = 84.50,
                type = "EXPENSE",
                note = "Family dinner at bistro",
                date = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false
            ),
            currencySymbol = "$",
            onDismiss = {},
            onEdit = {}
        )
    }
}
