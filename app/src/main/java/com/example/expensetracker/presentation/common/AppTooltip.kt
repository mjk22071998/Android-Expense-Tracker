package com.example.expensetracker.presentation.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class TooltipPosition {
    Above,
    Below,
    Left,
    Right
}

/**
 * Reusable, accessible tooltip wrapper styled for the Organic Forest Luxury theme.
 * Displays on long-press (touch screens) or pointer hover (accessibility/stylus/desktop).
 * Encapsulates Material 3 experimental tooltip internals so call sites remain clean.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTooltip(
    text: String,
    modifier: Modifier = Modifier,
    position: TooltipPosition = TooltipPosition.Above,
    content: @Composable () -> Unit
) {
    val anchor = when (position) {
        TooltipPosition.Above -> TooltipAnchorPosition.Above
        TooltipPosition.Below -> TooltipAnchorPosition.Below
        TooltipPosition.Left -> TooltipAnchorPosition.Left
        TooltipPosition.Right -> TooltipAnchorPosition.Right
    }
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(anchor),
        tooltip = {
            PlainTooltip(
                shape = RoundedCornerShape(8.dp),
                containerColor = Color(0xFF0D2418),
                contentColor = Color.White
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE4F5EB),
                    fontSize = 12.sp
                )
            }
        },
        state = tooltipState,
        modifier = modifier
    ) {
        content()
    }
}
