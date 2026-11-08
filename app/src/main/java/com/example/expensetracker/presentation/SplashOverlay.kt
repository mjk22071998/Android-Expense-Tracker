package com.example.expensetracker.presentation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.R
import com.example.expensetracker.ui.theme.ElectricLime
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun SplashOverlay() {
    val extendedColors = ExpenseTrackerTheme.extendedColors

    val splashGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F261B),
            Color(0xFF08140E)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(splashGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Glowing App Icon Avatar
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color(0x22D4F63D)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_receipt_long),
                    contentDescription = null,
                    tint = ElectricLime,
                    modifier = Modifier.size(48.dp)
                )
            }

            Text(
                text = "Expense Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = extendedColors.textPrimary,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Personal Financial Architecture",
                style = MaterialTheme.typography.labelSmall,
                color = extendedColors.textMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            CircularProgressIndicator(
                color = ElectricLime,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Preview(name = "Splash - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SplashOverlayPreview() {
    ExpenseTrackerTheme {
        SplashOverlay()
    }
}
