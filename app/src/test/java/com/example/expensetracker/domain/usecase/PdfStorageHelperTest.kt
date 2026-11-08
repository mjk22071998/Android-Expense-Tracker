package com.example.expensetracker.domain.usecase

import android.net.Uri
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfStorageHelperTest {

    @Test
    fun savedReportResult_holdsCorrectPhoneStorageProperties() {
        val mockUri = mockk<Uri>()
        val result = SavedReportResult(
            uri = mockUri,
            displayPath = "Phone Storage/Download/ExpenseTracker/Expense_Report_20261108_200000.pdf",
            isSdCard = false
        )

        assertEquals(mockUri, result.uri)
        assertEquals("Phone Storage/Download/ExpenseTracker/Expense_Report_20261108_200000.pdf", result.displayPath)
        assertFalse(result.isSdCard)
    }

    @Test
    fun savedReportResult_holdsCorrectSdCardProperties() {
        val mockUri = mockk<Uri>()
        val result = SavedReportResult(
            uri = mockUri,
            displayPath = "SD Card/Download/ExpenseTracker/Expense_Report_20261108_200000.pdf",
            isSdCard = true
        )

        assertEquals(mockUri, result.uri)
        assertEquals("SD Card/Download/ExpenseTracker/Expense_Report_20261108_200000.pdf", result.displayPath)
        assertTrue(result.isSdCard)
    }
}
