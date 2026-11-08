package com.example.expensetracker.presentation.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.Constants
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.domain.model.CurrencyHelper.formatAmount
import com.example.expensetracker.domain.model.getCategoryIcon
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionItem(
    transaction: TransactionWithCategory,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val isIncome = transaction.type == Constants.TransactionType.INCOME

    // Vibrant circular avatar matching Screen 1 (e.g. green Wise icon, category avatar)
    val avatarBg = if (isIncome) Color(0xFF1B4D2B) else Color(0x33FFFFFF)
    val avatarTint = if (isIncome) ElectricLime else Color.White

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Circular Avatar / Category Icon
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(avatarBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = getCategoryIcon(transaction.categoryIcon)),
                contentDescription = transaction.categoryName,
                tint = avatarTint,
                modifier = Modifier.size(22.dp)
            )
        }

        // Title & Timestamp
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = transaction.note.ifBlank { transaction.categoryName },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = buildString {
                    append(formatTransactionDate(transaction.date))
                    if (transaction.note.isNotBlank() && transaction.categoryName.isNotBlank()) {
                        append(" • ")
                        append(transaction.categoryName)
                    }
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFA5C4B2),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Amount & Subtitle (Deposit / Expense)
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "${if (isIncome) "+" else "-"} $currencySymbol ${formatAmount(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp,
                letterSpacing = (-0.3).sp
            )

            Text(
                text = if (isIncome) "Deposit" else "Expense",
                style = MaterialTheme.typography.labelSmall,
                color = if (isIncome) ElectricLime else Color(0xFFA5C4B2),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Contextual 3-dot Menu
        Box {
            IconButton(
                onClick = { isMenuExpanded = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_vert),
                    contentDescription = "More",
                    tint = Color(0xFFA5C4B2),
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit"
                        )
                    },
                    onClick = {
                        isMenuExpanded = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        isMenuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}

internal fun formatTransactionDate(dateMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault())
    return formatter.format(Date(dateMillis))
}

@Preview(name = "Transaction Item - Screen 1 Style", showBackground = true, backgroundColor = 0xFF0A1810)
@Composable
private fun TransactionItemPreview() {
    ExpenseTrackerTheme {
        TransactionItem(
            transaction = TransactionWithCategory(
                id = "1",
                ledgerId = "ledger-1",
                amount = 730.20,
                type = Constants.TransactionType.INCOME,
                date = System.currentTimeMillis(),
                note = "Wise Transfer",
                categoryId = "cat-salary",
                categoryName = "Income",
                categoryIcon = "ic_work",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false
            ),
            currencySymbol = "$",
            onEdit = {},
            onDelete = {},
            onClick = {}
        )
    }
}
