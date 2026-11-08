package com.example.expensetracker.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn

@Composable
fun QuickActionsRow(
    onAddTransaction: () -> Unit,
    onFilterPeriodClick: () -> Unit,
    onSettingsClick: () -> Unit,
    periodLabel: String = "This Month",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Primary Vibrant Yellow/Lime Pill (⇆ Transfer / Add Entry)
        Surface(
            onClick = onAddTransaction,
            modifier = Modifier
                .weight(1.3f)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            color = ElectricLime
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Entry",
                    tint = ElectricLimeOn,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = "Add Entry",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = ElectricLimeOn,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }

        // 2. Vibrant Cyan / Blue Pill (🎯 Set Target / Period Filter)
        Surface(
            onClick = onFilterPeriodClick,
            modifier = Modifier
                .weight(1.3f)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0099FF)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_date_range),
                    contentDescription = "Period",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = periodLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }

        // 3. Dark Circular Glass Button (⚙ Settings)
        Surface(
            onClick = onSettingsClick,
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = Color(0x28FFFFFF),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
