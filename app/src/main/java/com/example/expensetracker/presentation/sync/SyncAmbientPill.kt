package com.example.expensetracker.presentation.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun SyncAmbientPill(
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isSyncing,
        enter = fadeIn(animationSpec = tween(200)) + slideInHorizontally(initialOffsetX = { it / 2 }),
        exit = fadeOut(animationSpec = tween(200)) + slideOutHorizontally(targetOffsetX = { it / 2 }),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing)
            ),
            label = "sync_spin"
        )

        val extendedColors = ExpenseTrackerTheme.extendedColors
        val pillBg = if (extendedColors.isDark) Color(0x2A1B4D34) else Color(0x181B4D34)
        val pillBorder = if (extendedColors.isDark) Color(0x44D4F63D) else Color(0x331B4D34)
        val pillContent = if (extendedColors.isDark) ElectricLime else Color(0xFF1B4D34)

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = pillBg,
            border = BorderStroke(1.dp, pillBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sync),
                    contentDescription = "Syncing",
                    tint = pillContent,
                    modifier = Modifier
                        .size(13.dp)
                        .rotate(rotation)
                )
                Text(
                    text = "Syncing...",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = pillContent,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}
