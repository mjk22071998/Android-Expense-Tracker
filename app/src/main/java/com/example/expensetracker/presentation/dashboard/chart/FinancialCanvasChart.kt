package com.example.expensetracker.presentation.dashboard.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.domain.model.chart.ChartDataPoint
import com.example.expensetracker.domain.model.chart.ChartDataSet
import com.example.expensetracker.domain.model.chart.ChartMode
import com.example.expensetracker.ui.theme.LocalExtendedColors
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FinancialCanvasChart(
    dataSet: ChartDataSet,
    mode: ChartMode,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    var scrubbedIndex by remember(dataSet, mode) { mutableStateOf<Int?>(null) }

    val points = dataSet.points
    if (points.isEmpty()) {
        return
    }

    val topPaddingPx = with(density) { 26.dp.toPx() }
    val bottomPaddingPx = with(density) { 34.dp.toPx() }
    val leftPaddingPx = with(density) { 12.dp.toPx() }
    val rightPaddingPx = with(density) { 12.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(points, mode) {
                detectTapGestures(
                    onTap = { offset ->
                        val drawWidth = size.width - leftPaddingPx - rightPaddingPx
                        if (drawWidth > 0 && points.isNotEmpty()) {
                            val stepX = drawWidth / points.size
                            val touchX = (offset.x - leftPaddingPx).coerceIn(0f, drawWidth - 0.1f)
                            val idx = (touchX / stepX).toInt().coerceIn(0, points.size - 1)
                            if (scrubbedIndex != idx) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            scrubbedIndex = if (scrubbedIndex == idx) null else idx
                        }
                    }
                )
            }
            .pointerInput(points, mode) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        val drawWidth = size.width - leftPaddingPx - rightPaddingPx
                        if (drawWidth > 0 && points.isNotEmpty()) {
                            val stepX = drawWidth / points.size
                            val touchX = (offset.x - leftPaddingPx).coerceIn(0f, drawWidth - 0.1f)
                            val idx = (touchX / stepX).toInt().coerceIn(0, points.size - 1)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scrubbedIndex = idx
                        }
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val drawWidth = size.width - leftPaddingPx - rightPaddingPx
                        if (drawWidth > 0 && points.isNotEmpty()) {
                            val stepX = drawWidth / points.size
                            val touchX = (change.position.x - leftPaddingPx).coerceIn(0f, drawWidth - 0.1f)
                            val idx = (touchX / stepX).toInt().coerceIn(0, points.size - 1)
                            if (scrubbedIndex != idx) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                scrubbedIndex = idx
                            }
                        }
                    },
                    onDragEnd = {
                        scrubbedIndex = null
                    },
                    onDragCancel = {
                        scrubbedIndex = null
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val drawWidth = width - leftPaddingPx - rightPaddingPx
            val drawHeight = height - topPaddingPx - bottomPaddingPx
            val baseY = height - bottomPaddingPx

            val n = points.size
            if (n == 0 || drawWidth <= 0 || drawHeight <= 0) return@Canvas

            val stepX = drawWidth / n

            // ---------- 1. Draw 3 Horizontal Dashed Guidelines ----------
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
            val guidelineColor = if (extendedColors.isDark) Color(0x22FFFFFF) else Color(0x1F000000)

            // Top, Mid, and Base lines
            drawLine(
                color = guidelineColor,
                start = Offset(leftPaddingPx, topPaddingPx),
                end = Offset(width - rightPaddingPx, topPaddingPx),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect
            )
            drawLine(
                color = guidelineColor,
                start = Offset(leftPaddingPx, topPaddingPx + drawHeight / 2),
                end = Offset(width - rightPaddingPx, topPaddingPx + drawHeight / 2),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect
            )
            drawLine(
                color = if (extendedColors.isDark) Color(0x3DFFFFFF) else Color(0x3D000000),
                start = Offset(leftPaddingPx, baseY),
                end = Offset(width - rightPaddingPx, baseY),
                strokeWidth = 1.2.dp.toPx()
            )

            // ---------- 2. Mode Specific Chart Drawing ----------
            when (mode) {
                ChartMode.EXPENSES -> {
                    val maxVal = dataSet.maxExpense.coerceAtLeast(10.0)
                    val barWidth = when {
                        n <= 7 -> stepX * 0.42f
                        n <= 12 -> stepX * 0.48f
                        else -> (stepX * 0.65f).coerceIn(3.dp.toPx(), 14.dp.toPx())
                    }
                    val barRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    val gradientColors = listOf(
                        extendedColors.crimsonExpense,
                        Color(0xFF9F1239)
                    )

                    points.forEachIndexed { i, pt ->
                        val centerX = leftPaddingPx + (i + 0.5f) * stepX
                        val ratio = (pt.expense / maxVal).toFloat().coerceIn(0f, 1f)
                        val barHeight = (drawHeight * ratio).coerceAtLeast(0f)
                        val topY = baseY - barHeight

                        if (pt.expense > 0) {
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = gradientColors,
                                    startY = topY,
                                    endY = baseY
                                ),
                                topLeft = Offset(centerX - barWidth / 2f, topY),
                                size = Size(barWidth, barHeight),
                                cornerRadius = barRadius
                            )
                        } else {
                            // Zero-activity expense: subtle baseline dot
                            drawCircle(
                                color = if (extendedColors.isDark) Color(0x40FFFFFF) else Color(0x2E000000),
                                radius = 2.dp.toPx(),
                                center = Offset(centerX, baseY)
                            )
                        }
                    }
                }

                ChartMode.INCOME -> {
                    val maxVal = dataSet.maxIncome.coerceAtLeast(10.0)
                    val barWidth = when {
                        n <= 7 -> stepX * 0.42f
                        n <= 12 -> stepX * 0.48f
                        else -> (stepX * 0.65f).coerceIn(3.dp.toPx(), 14.dp.toPx())
                    }
                    val barRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    val gradientColors = listOf(
                        extendedColors.emeraldIncome,
                        Color(0xFF047857)
                    )

                    points.forEachIndexed { i, pt ->
                        val centerX = leftPaddingPx + (i + 0.5f) * stepX
                        val ratio = (pt.income / maxVal).toFloat().coerceIn(0f, 1f)
                        val barHeight = (drawHeight * ratio).coerceAtLeast(0f)
                        val topY = baseY - barHeight

                        if (pt.income > 0) {
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = gradientColors,
                                    startY = topY,
                                    endY = baseY
                                ),
                                topLeft = Offset(centerX - barWidth / 2f, topY),
                                size = Size(barWidth, barHeight),
                                cornerRadius = barRadius
                            )
                        } else {
                            // Zero-activity income: subtle baseline dot
                            drawCircle(
                                color = if (extendedColors.isDark) Color(0x40FFFFFF) else Color(0x2E000000),
                                radius = 2.dp.toPx(),
                                center = Offset(centerX, baseY)
                            )
                        }
                    }
                }

                ChartMode.BALANCE -> {
                    val maxB = dataSet.maxBalance
                    val minB = dataSet.minBalance
                    val range = (maxB - minB).coerceAtLeast(10.0)
                    val yPadding = range * 0.12
                    val yMax = maxB + yPadding
                    val yMin = minB - yPadding
                    val ySpan = (yMax - yMin).coerceAtLeast(1.0)

                    val coords = points.mapIndexed { i, pt ->
                        val cx = leftPaddingPx + (i + 0.5f) * stepX
                        val normalized = ((pt.runningBalance - yMin) / ySpan).toFloat().coerceIn(0f, 1f)
                        val cy = baseY - (normalized * drawHeight)
                        Offset(cx, cy)
                    }

                    if (coords.isNotEmpty()) {
                        // Draw Area Glow
                        val areaPath = Path().apply {
                            moveTo(coords.first().x, baseY)
                            lineTo(coords.first().x, coords.first().y)
                            for (i in 0 until coords.size - 1) {
                                val p0 = coords[i]
                                val p1 = coords[i + 1]
                                val midX = (p0.x + p1.x) / 2f
                                cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                            }
                            lineTo(coords.last().x, baseY)
                            close()
                        }

                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    extendedColors.electricLime.copy(alpha = 0.28f),
                                    Color.Transparent
                                ),
                                startY = topPaddingPx,
                                endY = baseY
                            )
                        )

                        // Draw Curve Line
                        val linePath = Path().apply {
                            moveTo(coords.first().x, coords.first().y)
                            for (i in 0 until coords.size - 1) {
                                val p0 = coords[i]
                                val p1 = coords[i + 1]
                                val midX = (p0.x + p1.x) / 2f
                                cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                            }
                        }

                        drawPath(
                            path = linePath,
                            color = extendedColors.electricLime,
                            style = Stroke(
                                width = 2.8.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Draw Point Dots
                        coords.forEachIndexed { i, c ->
                            val pt = points[i]
                            val isScrubbed = scrubbedIndex == i
                            if (isScrubbed || n <= 14) {
                                drawCircle(
                                    color = if (extendedColors.isDark) Color(0xFF08140E) else Color.White,
                                    radius = 4.dp.toPx(),
                                    center = c
                                )
                                drawCircle(
                                    color = extendedColors.electricLime,
                                    radius = 2.5.dp.toPx(),
                                    center = c
                                )
                            }
                        }
                    }
                }
            }

            // ---------- 3. X-Axis Labels ----------
            val labelStyle = TextStyle(
                color = extendedColors.textSecondary.copy(alpha = 0.85f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            val showInterval = when {
                n <= 7 -> 1
                n <= 14 -> 2
                n <= 31 -> 5
                else -> 1
            }

            points.forEachIndexed { i, pt ->
                val shouldShow = when {
                    n <= 7 -> true
                    n <= 14 -> i % 2 == 0 || i == n - 1
                    n <= 31 -> (i + 1) == 1 || (i + 1) % 5 == 0 || i == n - 1
                    else -> true
                }

                if (shouldShow) {
                    val cx = leftPaddingPx + (i + 0.5f) * stepX
                    val textLayout = textMeasurer.measure(pt.label, labelStyle)
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(
                            cx - textLayout.size.width / 2f,
                            baseY + 8.dp.toPx()
                        )
                    )
                }
            }

            // ---------- 4. Active Touch-Scrubber Vertical Crosshair ----------
            scrubbedIndex?.let { idx ->
                if (idx in points.indices) {
                    val cx = leftPaddingPx + (idx + 0.5f) * stepX
                    val accentColor = when (mode) {
                        ChartMode.EXPENSES -> extendedColors.crimsonExpense
                        ChartMode.INCOME -> extendedColors.emeraldIncome
                        ChartMode.BALANCE -> extendedColors.electricLime
                    }

                    // Vertical crosshair guideline
                    drawLine(
                        color = accentColor.copy(alpha = 0.65f),
                        start = Offset(cx, topPaddingPx),
                        end = Offset(cx, baseY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )

                    // Crosshair marker ring
                    val markerY = when (mode) {
                        ChartMode.EXPENSES -> {
                            val maxVal = dataSet.maxExpense.coerceAtLeast(10.0)
                            baseY - (drawHeight * (points[idx].expense / maxVal).toFloat().coerceIn(0f, 1f))
                        }
                        ChartMode.INCOME -> {
                            val maxVal = dataSet.maxIncome.coerceAtLeast(10.0)
                            baseY - (drawHeight * (points[idx].income / maxVal).toFloat().coerceIn(0f, 1f))
                        }
                        ChartMode.BALANCE -> {
                            val maxB = dataSet.maxBalance
                            val minB = dataSet.minBalance
                            val range = (maxB - minB).coerceAtLeast(10.0)
                            val yPadding = range * 0.12
                            val yMax = maxB + yPadding
                            val yMin = minB - yPadding
                            val ySpan = (yMax - yMin).coerceAtLeast(1.0)
                            baseY - (((points[idx].runningBalance - yMin) / ySpan).toFloat().coerceIn(0f, 1f) * drawHeight)
                        }
                    }

                    drawCircle(
                        color = accentColor.copy(alpha = 0.35f),
                        radius = 8.dp.toPx(),
                        center = Offset(cx, markerY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(cx, markerY)
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 2.5.dp.toPx(),
                        center = Offset(cx, markerY)
                    )
                }
            }
        }

        // ---------- 5. Floating Interactive Tooltip Pill ----------
        scrubbedIndex?.let { idx ->
            if (idx in points.indices) {
                val pt = points[idx]
                val amountText = when (mode) {
                    ChartMode.EXPENSES -> "$currencySymbol ${formatAmount(pt.expense)}"
                    ChartMode.INCOME -> "$currencySymbol ${formatAmount(pt.income)}"
                    ChartMode.BALANCE -> "$currencySymbol ${formatAmount(pt.runningBalance)}"
                }
                val modeLabel = when (mode) {
                    ChartMode.EXPENSES -> "Expense"
                    ChartMode.INCOME -> "Income"
                    ChartMode.BALANCE -> "Balance"
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (extendedColors.isDark) Color(0xEE163825) else Color(0xEEF0F8F3),
                    shadowElevation = 4.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (mode) {
                            ChartMode.EXPENSES -> extendedColors.crimsonExpense.copy(alpha = 0.5f)
                            ChartMode.INCOME -> extendedColors.emeraldIncome.copy(alpha = 0.5f)
                            ChartMode.BALANCE -> extendedColors.electricLime.copy(alpha = 0.5f)
                        }
                    )
                ) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(
                            text = "${pt.fullLabel} • $modeLabel: $amountText",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (extendedColors.isDark) Color.White else extendedColors.textPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    val rounded = (amount * 100.0).roundToInt() / 100.0
    return if (rounded % 1.0 == 0.0) {
        String.format(java.util.Locale.getDefault(), "%,d", rounded.toLong())
    } else {
        String.format(java.util.Locale.getDefault(), "%,.2f", rounded)
    }
}
