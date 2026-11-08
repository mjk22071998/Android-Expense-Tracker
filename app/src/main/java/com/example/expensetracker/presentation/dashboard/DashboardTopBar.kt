package com.example.expensetracker.presentation.dashboard

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ElectricLimeOn
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    activeCategoryFilterCount: Int,
    onSearchQueryChanged: (String) -> Unit,
    onSearchActivated: () -> Unit,
    onSearchClosed: () -> Unit,
    onFilterClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    if (isSearchActive) {
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }

        TopAppBar(
            title = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(extendedColors.cardBackground)
                        .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(24.dp))
                        .padding(horizontal = 4.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = {
                            Text(
                                "Search note, category, or amount...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = extendedColors.textMuted
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = extendedColors.textPrimary,
                            unfocusedTextColor = extendedColors.textPrimary
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onSearchClosed) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Close search",
                        tint = extendedColors.textPrimary
                    )
                }
            },
            actions = {
                AnimatedVisibility(visible = searchQuery.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Clear search",
                            tint = extendedColors.textSecondary
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
    } else {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Expense Tracker",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary,
                        letterSpacing = (-0.3).sp
                    )
                }
            },
            actions = {
                // Search Action
                IconButton(
                    onClick = onSearchActivated,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search",
                        tint = extendedColors.textPrimary
                    )
                }

                // Category Filter with Electric Lime Badge
                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .clip(CircleShape)
                ) {
                    BadgedBox(
                        badge = {
                            if (activeCategoryFilterCount > 0) {
                                Badge(
                                    containerColor = ElectricLime,
                                    contentColor = ElectricLimeOn
                                ) {
                                    Text(
                                        text = activeCategoryFilterCount.toString(),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filter_list),
                            contentDescription = "Filter by category",
                            tint = if (activeCategoryFilterCount > 0) ElectricLime else extendedColors.textPrimary
                        )
                    }
                }

                // Settings Action
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.clip(CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = extendedColors.textPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
    }
}

@Preview(name = "TopBar Inactive - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "TopBar Inactive - Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun DashboardTopBarPreview() {
    ExpenseTrackerTheme {
        DashboardTopBar(
            isSearchActive = false,
            searchQuery = "",
            activeCategoryFilterCount = 2,
            onSearchQueryChanged = {},
            onSearchActivated = {},
            onSearchClosed = {},
            onFilterClick = {},
            onSettingsClick = {}
        )
    }
}
