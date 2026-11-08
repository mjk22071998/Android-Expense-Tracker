package com.example.expensetracker.presentation.dashboard

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Surface
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
import com.example.expensetracker.ui.theme.ForestBorderDark
import com.example.expensetracker.ui.theme.ForestCardDark
import com.example.expensetracker.ui.theme.ForestTextMutedDark
import com.example.expensetracker.ui.theme.ForestTextPrimaryDark
import com.example.expensetracker.ui.theme.ForestTextSecondaryDark

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
    if (isSearchActive) {
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }

        TopAppBar(
            title = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = ForestCardDark,
                    border = BorderStroke(1.dp, ForestBorderDark)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = {
                            Text(
                                "Search note, category, or amount...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ForestTextMutedDark
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = ForestTextPrimaryDark,
                            unfocusedTextColor = ForestTextPrimaryDark
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
                        tint = ForestTextPrimaryDark
                    )
                }
            },
            actions = {
                AnimatedVisibility(visible = searchQuery.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Clear search",
                            tint = ForestTextSecondaryDark
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
            navigationIcon = {
                // Squircle account / ledger pill button from Inspiration Screen 1
                Surface(
                    modifier = Modifier
                        .padding(start = 14.dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    color = ForestCardDark,
                    border = BorderStroke(1.dp, ForestBorderDark)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_savings),
                            contentDescription = "Account",
                            tint = ElectricLime,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            title = {
                Row(
                    modifier = Modifier.padding(start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Personal Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForestTextPrimaryDark,
                        letterSpacing = (-0.2).sp
                    )
                }
            },
            actions = {
                // Category Filter Pill with Lime Badge
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onFilterClick),
                    shape = RoundedCornerShape(14.dp),
                    color = ForestCardDark,
                    border = BorderStroke(1.dp, ForestBorderDark)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
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
                                contentDescription = "Filter categories",
                                tint = ForestTextPrimaryDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Grid / Settings Squircle button
                Surface(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 14.dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onSettingsClick),
                    shape = RoundedCornerShape(14.dp),
                    color = ForestCardDark,
                    border = BorderStroke(1.dp, ForestBorderDark)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings",
                            tint = ForestTextSecondaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
    }
}

@Preview(name = "Dashboard Top Bar - Normal", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardTopBarNormalPreview() {
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
