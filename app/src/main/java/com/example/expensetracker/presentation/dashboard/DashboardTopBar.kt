package com.example.expensetracker.presentation.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.presentation.common.AppTooltip
import com.example.expensetracker.presentation.common.TooltipPosition
import com.example.expensetracker.presentation.sync.SyncAmbientPill
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    title: String = "Personal Account",
    activeLedgerName: String? = null,
    onLedgerSwitcherClick: (() -> Unit)? = null,
    isSyncing: Boolean = false,
    onExportClick: (() -> Unit)? = null
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    TopAppBar(
        title = {
            if (activeLedgerName != null && onLedgerSwitcherClick != null) {
                Surface(
                    onClick = onLedgerSwitcherClick,
                    shape = RoundedCornerShape(20.dp),
                    color = if (extendedColors.isDark) Color(0x28FFFFFF) else Color(0xFFFFFFFF),
                    border = BorderStroke(
                        1.dp,
                        if (extendedColors.isDark) Color(0x33FFFFFF) else extendedColors.cardBorder
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_account_balance_wallet),
                            contentDescription = null,
                            tint = extendedColors.emeraldIncome,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeLedgerName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (extendedColors.isDark) Color.White else extendedColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_expand_more),
                            contentDescription = "Switch Ledger",
                            tint = if (extendedColors.isDark) Color(0xB3FFFFFF) else extendedColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (extendedColors.isDark) Color.White else extendedColors.textPrimary,
                    letterSpacing = (-0.2).sp
                )
            }
        },
        actions = {
            SyncAmbientPill(
                isSyncing = isSyncing,
                modifier = Modifier.padding(end = 8.dp)
            )

            if (onExportClick != null) {
                AppTooltip(
                    text = "Export PDF Statement",
                    position = TooltipPosition.Below
                ) {
                    Surface(
                        onClick = onExportClick,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(38.dp),
                        shape = CircleShape,
                        color = if (extendedColors.isDark) Color(0x28FFFFFF) else Color(0xFFFFFFFF),
                        border = BorderStroke(
                            1.dp,
                            if (extendedColors.isDark) Color(0x33FFFFFF) else extendedColors.cardBorder
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_picture_as_pdf),
                                contentDescription = "Export to PDF",
                                tint = if (extendedColors.isDark) Color.White else extendedColors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Preview(name = "Dashboard Top Bar - Clean", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardTopBarPreview() {
    ExpenseTrackerTheme {
        DashboardTopBar(
            title = "Personal Account",
            activeLedgerName = "Personal",
            onLedgerSwitcherClick = {},
            onExportClick = {}
        )
    }
}
