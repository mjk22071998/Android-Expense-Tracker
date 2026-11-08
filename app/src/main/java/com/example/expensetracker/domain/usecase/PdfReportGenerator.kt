package com.example.expensetracker.domain.usecase

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.entity.TransactionWithCategory
import com.example.expensetracker.domain.model.CurrencyHelper.formatAmount
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // A4 width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 height in points (72 dpi)
    private const val MARGIN_LEFT = 36f
    private const val MARGIN_RIGHT = 559f
    private const val MARGIN_TOP = 36f
    private const val MARGIN_BOTTOM = 806f
    private const val CONTENT_WIDTH = MARGIN_RIGHT - MARGIN_LEFT // 523f

    private const val ROW_HEIGHT = 22f
    private const val PAGE_1_ROWS_CAPACITY = 25
    private const val SUBSEQUENT_PAGE_ROWS_CAPACITY = 30

    fun generate(
        context: Context,
        transactions: List<TransactionWithCategory>,
        ledgerName: String,
        currencySymbol: String,
        filterDescription: String
    ): Result<File> = runCatching {
        val pdfDocument = PdfDocument()

        val totalPages = calculateTotalPages(transactions.size)
        val now = System.currentTimeMillis()
        val generationDateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(now))
        val rowDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val totalIncome = transactions
            .filter { it.type == Constants.TransactionType.INCOME }
            .sumOf { it.amount }
        val totalExpense = transactions
            .filter { it.type == Constants.TransactionType.EXPENSE }
            .sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        var currentTransactionIndex = 0

        for (pageNumber in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var currentY: Float

            if (pageNumber == 1) {
                // Page 1 Full Header & Executive Summary
                currentY = drawPage1Header(
                    canvas = canvas,
                    generationDate = generationDateStr,
                    ledgerName = ledgerName,
                    filterDescription = filterDescription
                )

                currentY = drawExecutiveSummary(
                    canvas = canvas,
                    startY = currentY,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    netBalance = netBalance,
                    currencySymbol = currencySymbol,
                    transactionCount = transactions.size
                )
            } else {
                // Subsequent Page Compact Header
                currentY = drawCompactHeader(canvas = canvas, pageNumber = pageNumber)
            }

            // Draw Table Header
            currentY = drawTableHeader(canvas = canvas, startY = currentY)

            // Draw Table Rows for this page
            val rowsForThisPage = if (pageNumber == 1) PAGE_1_ROWS_CAPACITY else SUBSEQUENT_PAGE_ROWS_CAPACITY
            val endTransactionIndex = (currentTransactionIndex + rowsForThisPage).coerceAtMost(transactions.size)

            if (transactions.isEmpty()) {
                drawEmptyState(canvas, currentY)
            } else {
                for (i in currentTransactionIndex until endTransactionIndex) {
                    val txn = transactions[i]
                    val isAlternate = (i % 2 == 1)
                    drawTableRow(
                        canvas = canvas,
                        startY = currentY,
                        transaction = txn,
                        currencySymbol = currencySymbol,
                        dateFormat = rowDateFormat,
                        isAlternate = isAlternate
                    )
                    currentY += ROW_HEIGHT
                }
            }
            currentTransactionIndex = endTransactionIndex

            // Draw Footer
            drawFooter(canvas = canvas, pageNumber = pageNumber, totalPages = totalPages)

            pdfDocument.finishPage(page)
        }

        // Save to cacheDir/reports/
        val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
        val reportFile = File(reportsDir, "Expense_Report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(reportFile).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()

        reportFile
    }

    private fun calculateTotalPages(transactionCount: Int): Int {
        if (transactionCount <= PAGE_1_ROWS_CAPACITY) return 1
        val remaining = transactionCount - PAGE_1_ROWS_CAPACITY
        return 1 + ceil(remaining.toDouble() / SUBSEQUENT_PAGE_ROWS_CAPACITY).toInt()
    }

    private fun drawPage1Header(
        canvas: Canvas,
        generationDate: String,
        ledgerName: String,
        filterDescription: String
    ): Float {
        val titlePaint = Paint().apply {
            color = Color.rgb(14, 32, 23) // Obsidian Forest
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(90, 107, 97) // Slate Forest Muted
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.rgb(60, 75, 67)
            textSize = 8.5f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        // Left Branding
        canvas.drawText("EXPENSE TRACKER", MARGIN_LEFT, 56f, titlePaint)
        canvas.drawText("Financial Statement Report", MARGIN_LEFT, 70f, subtitlePaint)

        // Right Metadata
        canvas.drawText("Account: $ledgerName", MARGIN_RIGHT, 54f, metaPaint)
        canvas.drawText("Generated: $generationDate", MARGIN_RIGHT, 68f, metaPaint)

        // Filter scope chip / text
        val filterPaint = Paint().apply {
            color = Color.rgb(46, 99, 59)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Filter: $filterDescription", MARGIN_LEFT, 88f, filterPaint)

        // Divider Line
        val dividerPaint = Paint().apply {
            color = Color.rgb(226, 235, 230)
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN_LEFT, 98f, MARGIN_RIGHT, 98f, dividerPaint)

        return 108f
    }

    private fun drawCompactHeader(canvas: Canvas, pageNumber: Int): Float {
        val headerPaint = Paint().apply {
            color = Color.rgb(90, 107, 97)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("EXPENSE TRACKER — Financial Statement (Page $pageNumber)", MARGIN_LEFT, 52f, headerPaint)

        val dividerPaint = Paint().apply {
            color = Color.rgb(226, 235, 230)
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN_LEFT, 60f, MARGIN_RIGHT, 60f, dividerPaint)

        return 70f
    }

    private fun drawExecutiveSummary(
        canvas: Canvas,
        startY: Float,
        totalIncome: Double,
        totalExpense: Double,
        netBalance: Double,
        currencySymbol: String,
        transactionCount: Int
    ): Float {
        val cardHeight = 48f
        val cardWidth = (CONTENT_WIDTH - 16f) / 3f

        // Card 1: Total Inflow
        drawSummaryCard(
            canvas = canvas,
            rect = RectF(MARGIN_LEFT, startY, MARGIN_LEFT + cardWidth, startY + cardHeight),
            label = "TOTAL INFLOW",
            value = "+ $currencySymbol ${formatAmount(totalIncome)}",
            valueColor = Color.rgb(16, 185, 129), // Emerald
            bgColor = Color.rgb(240, 253, 244)
        )

        // Card 2: Total Outflow
        val card2X = MARGIN_LEFT + cardWidth + 8f
        drawSummaryCard(
            canvas = canvas,
            rect = RectF(card2X, startY, card2X + cardWidth, startY + cardHeight),
            label = "TOTAL OUTFLOW",
            value = "− $currencySymbol ${formatAmount(totalExpense)}",
            valueColor = Color.rgb(244, 63, 94), // Crimson
            bgColor = Color.rgb(255, 241, 242)
        )

        // Card 3: Net Balance
        val card3X = card2X + cardWidth + 8f
        val balancePrefix = if (netBalance >= 0) "+ " else "− "
        drawSummaryCard(
            canvas = canvas,
            rect = RectF(card3X, startY, card3X + cardWidth, startY + cardHeight),
            label = "NET BALANCE ($transactionCount)",
            value = "$balancePrefix$currencySymbol ${formatAmount(kotlin.math.abs(netBalance))}",
            valueColor = if (netBalance >= 0) Color.rgb(14, 32, 23) else Color.rgb(244, 63, 94),
            bgColor = Color.rgb(244, 247, 245)
        )

        return startY + cardHeight + 14f
    }

    private fun drawSummaryCard(
        canvas: Canvas,
        rect: RectF,
        label: String,
        value: String,
        valueColor: Int,
        bgColor: Int
    ) {
        val bgPaint = Paint().apply {
            color = bgColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(220, 232, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        canvas.drawRoundRect(rect, 8f, 8f, bgPaint)
        canvas.drawRoundRect(rect, 8f, 8f, borderPaint)

        val labelPaint = Paint().apply {
            color = Color.rgb(100, 116, 107)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = valueColor
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText(label, rect.left + 10f, rect.top + 16f, labelPaint)
        canvas.drawText(value, rect.left + 10f, rect.top + 34f, valuePaint)
    }

    private fun drawTableHeader(canvas: Canvas, startY: Float): Float {
        val headerHeight = 20f
        val rect = RectF(MARGIN_LEFT, startY, MARGIN_RIGHT, startY + headerHeight)

        val bgPaint = Paint().apply {
            color = Color.rgb(238, 243, 240)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, 4f, 4f, bgPaint)

        val headerTextPaint = Paint().apply {
            color = Color.rgb(40, 55, 46)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val amountHeaderPaint = Paint(headerTextPaint).apply {
            textAlign = Paint.Align.RIGHT
        }

        val textY = startY + 13.5f
        canvas.drawText("DATE", MARGIN_LEFT + 8f, textY, headerTextPaint)
        canvas.drawText("CATEGORY", MARGIN_LEFT + 95f, textY, headerTextPaint)
        canvas.drawText("DESCRIPTION", MARGIN_LEFT + 195f, textY, headerTextPaint)
        canvas.drawText("TYPE", MARGIN_LEFT + 365f, textY, headerTextPaint)
        canvas.drawText("AMOUNT", MARGIN_RIGHT - 8f, textY, amountHeaderPaint)

        return startY + headerHeight + 2f
    }

    private fun drawTableRow(
        canvas: Canvas,
        startY: Float,
        transaction: TransactionWithCategory,
        currencySymbol: String,
        dateFormat: SimpleDateFormat,
        isAlternate: Boolean
    ) {
        val rowRect = RectF(MARGIN_LEFT, startY, MARGIN_RIGHT, startY + ROW_HEIGHT)

        if (isAlternate) {
            val bgPaint = Paint().apply {
                color = Color.rgb(249, 252, 250)
                style = Paint.Style.FILL
            }
            canvas.drawRect(rowRect, bgPaint)
        }

        // Underline border
        val linePaint = Paint().apply {
            color = Color.rgb(238, 242, 239)
            strokeWidth = 0.5f
        }
        canvas.drawLine(MARGIN_LEFT, startY + ROW_HEIGHT, MARGIN_RIGHT, startY + ROW_HEIGHT, linePaint)

        val textPaint = Paint().apply {
            color = Color.rgb(30, 41, 35)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val isIncome = transaction.type == Constants.TransactionType.INCOME

        val typeBadgePaint = Paint().apply {
            color = if (isIncome) Color.rgb(16, 185, 129) else Color.rgb(244, 63, 94)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val amountPaint = Paint().apply {
            color = if (isIncome) Color.rgb(16, 185, 129) else Color.rgb(244, 63, 94)
            textSize = 8.5f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textY = startY + 14.5f

        // Date
        canvas.drawText(dateFormat.format(Date(transaction.date)), MARGIN_LEFT + 8f, textY, textPaint)

        // Category
        val catText = truncate(transaction.categoryName, 18)
        canvas.drawText(catText, MARGIN_LEFT + 95f, textY, textPaint)

        // Note/Description
        val noteText = if (transaction.note.isNotBlank()) truncate(transaction.note, 30) else "—"
        canvas.drawText(noteText, MARGIN_LEFT + 195f, textY, textPaint)

        // Type
        canvas.drawText(if (isIncome) "INFLOW" else "OUTFLOW", MARGIN_LEFT + 365f, textY, typeBadgePaint)

        // Amount
        val sign = if (isIncome) "+ " else "− "
        val formattedAmount = "$sign$currencySymbol ${formatAmount(transaction.amount)}"
        canvas.drawText(formattedAmount, MARGIN_RIGHT - 8f, textY, amountPaint)
    }

    private fun drawEmptyState(canvas: Canvas, startY: Float) {
        val emptyPaint = Paint().apply {
            color = Color.rgb(120, 135, 127)
            textSize = 9.5f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawText(
            "No transactions matched the selected filters.",
            PAGE_WIDTH / 2f,
            startY + 40f,
            emptyPaint
        )
    }

    private fun drawFooter(canvas: Canvas, pageNumber: Int, totalPages: Int) {
        val linePaint = Paint().apply {
            color = Color.rgb(226, 235, 230)
            strokeWidth = 0.5f
        }
        canvas.drawLine(MARGIN_LEFT, MARGIN_BOTTOM - 14f, MARGIN_RIGHT, MARGIN_BOTTOM - 14f, linePaint)

        val footerPaint = Paint().apply {
            color = Color.rgb(120, 135, 127)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val pageNumberPaint = Paint(footerPaint).apply {
            textAlign = Paint.Align.RIGHT
        }

        canvas.drawText("Expense Tracker — Personal Finance Statement • Confidential", MARGIN_LEFT, MARGIN_BOTTOM, footerPaint)
        canvas.drawText("Page $pageNumber of $totalPages", MARGIN_RIGHT, MARGIN_BOTTOM, pageNumberPaint)
    }

    private fun truncate(text: String, maxChars: Int): String {
        return if (text.length > maxChars) text.take(maxChars - 1) + "…" else text
    }
}
