package com.example.expensetracker.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetracker.R
import com.example.expensetracker.ui.theme.ElectricCyan
import com.example.expensetracker.ui.theme.ElectricCyanOn
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ForestBorderDark
import com.example.expensetracker.ui.theme.ForestCardDark
import com.example.expensetracker.ui.theme.ForestTextPrimaryDark

@Composable
fun QuickActionsRow(
    onAddTransaction: () -> Unit,
    onFilterCategories: () -> Unit,
    onSettingsClick: () -> Unit,
    activeCategoryFilterCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Primary Lime Action Pill: + Add Entry
        Surface(
            modifier = Modifier
                .weight(1.3f)
                .height(46.dp)
                .clip(RoundedCornerShape(23.dp))
                .clickable(onClick = onAddTransaction),
            shape = RoundedCornerShape(23.dp),
            color = ElectricLime,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Transaction",
                    tint = ElectricLimeOn,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Add Entry",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = ElectricLimeOn,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }

        // Secondary Cyan Action Pill: Filter Categories
        Surface(
            modifier = Modifier
                .weight(1.3f)
                .height(46.dp)
                .clip(RoundedCornerShape(23.dp))
                .clickable(onClick = onFilterCategories),
            shape = RoundedCornerShape(23.dp),
            color = ElectricCyan,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_filter_list),
                    contentDescription = "Filter",
                    tint = ElectricCyanOn,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (activeCategoryFilterCount > 0) "Filtered ($activeCategoryFilterCount)" else "Categories",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyanOn,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }

        // Circle Dark Glass Settings Button
        Surface(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .clickable(onClick = onSettingsClick),
            shape = CircleShape,
            color = ForestCardDark,
            border = BorderStroke(1.dp, ForestBorderDark),
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings",
                    tint = ForestTextPrimaryDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
