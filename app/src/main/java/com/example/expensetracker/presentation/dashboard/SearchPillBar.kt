package com.example.expensetracker.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.presentation.common.AppTooltip
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun SearchPillBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        color = if (extendedColors.isDark) Color(0x4012351E) else Color(0xFFFFFFFF),
        border = BorderStroke(
            1.dp,
            if (extendedColors.isDark) Color(0x28FFFFFF) else extendedColors.cardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                tint = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary,
                modifier = Modifier.size(19.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search Transactions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textMuted,
                        fontSize = 14.sp
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = if (extendedColors.isDark) Color.White else extendedColors.textPrimary,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (query.isNotEmpty()) {
                AppTooltip(
                    text = "Clear search"
                ) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Clear",
                            tint = if (extendedColors.isDark) Color(0xFFA5C4B2) else extendedColors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
