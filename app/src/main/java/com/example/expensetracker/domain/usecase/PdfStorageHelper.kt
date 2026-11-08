package com.example.expensetracker.domain.usecase

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SavedReportResult(
    val uri: Uri,
    val displayPath: String,
    val isSdCard: Boolean
)

object PdfStorageHelper {

    private const val TAG = "PdfStorageHelper"

    /**
     * Checks if the app has required storage permissions for direct file writing.
     * Android 10+ (API 29+) enforces Scoped Storage and MediaStore requires 0 permissions.
     * Android 9 and lower (API 24-28) requires WRITE_EXTERNAL_STORAGE.
     */
    fun hasStoragePermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true
        }
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun saveReportToStorage(
        context: Context,
        pdfFile: File,
        baseName: String = "Expense_Report"
    ): Result<SavedReportResult> = runCatching {
        Log.d(TAG, "saveReportToStorage called: file=${pdfFile.absolutePath}, size=${pdfFile.length()}")
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${baseName}_$timeStamp.pdf"

        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
        val volumes = storageManager?.storageVolumes.orEmpty()
        val removableVolume = volumes.firstOrNull { it.isRemovable && it.state == Environment.MEDIA_MOUNTED }
        Log.d(TAG, "Removable volume: ${removableVolume?.uuid}, state=${removableVolume?.state}")

        // Check if OEM system settings explicitly prefer external SD card
        var isSdCardPreferred = false
        try {
            val pref1 = Settings.System.getString(context.contentResolver, "storage_default_path")
            val pref2 = Settings.System.getString(context.contentResolver, "preferred_storage_location")
            val pref3 = Settings.Global.getString(context.contentResolver, "default_storage")
            val combined = "$pref1 $pref2 $pref3".lowercase(Locale.ROOT)
            if (combined.contains("sd") || combined.contains("external")) {
                isSdCardPreferred = true
            }
        } catch (_: Exception) {
            // Permission or setting key not available on this OEM ROM; fallback gracefully
        }
        Log.d(TAG, "isSdCardPreferred: $isSdCardPreferred")

        // If SD card is mounted and preferred (or present on Android 11+), attempt saving to SD card volume
        if (removableVolume != null && (isSdCardPreferred || Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)) {
            val sdResult = trySaveToSdCard(context, pdfFile, fileName)
            if (sdResult != null) {
                Log.d(TAG, "Saved to SD card successfully: ${sdResult.displayPath}")
                return@runCatching sdResult
            }
        }

        // Primary fallback: Phone Storage
        val primaryResult = saveToPrimaryStorage(context, pdfFile, fileName)
        Log.d(TAG, "Saved to primary storage: ${primaryResult.displayPath}, uri=${primaryResult.uri}")
        primaryResult
    }.onFailure { err ->
        Log.e(TAG, "Failed in saveReportToStorage", err)
    }

    private fun trySaveToSdCard(
        context: Context,
        pdfFile: File,
        fileName: String
    ): SavedReportResult? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val volumeNames = MediaStore.getExternalVolumeNames(context)
                val sdVolume = volumeNames.firstOrNull {
                    it != MediaStore.VOLUME_EXTERNAL_PRIMARY && it != "internal"
                }
                if (sdVolume != null) {
                    val contentUri = MediaStore.Downloads.getContentUri(sdVolume)
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/ExpenseTracker/")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                    val uri = context.contentResolver.insert(contentUri, values)
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            pdfFile.inputStream().use { input -> input.copyTo(out) }
                        }
                        values.clear()
                        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        context.contentResolver.update(uri, values, null, null)

                        return SavedReportResult(
                            uri = uri,
                            displayPath = "SD Card/Download/ExpenseTracker/$fileName",
                            isSdCard = true
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "trySaveToSdCard API 30+ failed", e)
            }
        } else {
            // Android 9/10 with external files dirs check
            try {
                val externalDirs = ContextCompat.getExternalFilesDirs(context, null)
                if (externalDirs.size > 1 && externalDirs[1] != null) {
                    val sdCardRoot = externalDirs[1]!!.absolutePath.substringBefore("/Android/")
                    val targetDir = File(sdCardRoot, "Download/ExpenseTracker").apply { if (!exists()) mkdirs() }
                    val destFile = File(targetDir, fileName)
                    pdfFile.copyTo(destFile, overwrite = true)
                    MediaScannerConnection.scanFile(context, arrayOf(destFile.absolutePath), arrayOf("application/pdf"), null)
                    return SavedReportResult(
                        uri = Uri.fromFile(destFile),
                        displayPath = "SD Card/Download/ExpenseTracker/$fileName",
                        isSdCard = true
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "trySaveToSdCard legacy failed", e)
            }
        }
        return null
    }

    private fun saveToPrimaryStorage(
        context: Context,
        pdfFile: File,
        fileName: String
    ): SavedReportResult {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/ExpenseTracker/")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Could not create download entry in phone storage")

            Log.d(TAG, "MediaStore insert succeeded: $uri. Writing bytes...")
            context.contentResolver.openOutputStream(uri)?.use { out ->
                pdfFile.inputStream().use { input -> input.copyTo(out) }
            }

            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)

            return SavedReportResult(
                uri = uri,
                displayPath = "Phone Storage/Download/ExpenseTracker/$fileName",
                isSdCard = false
            )
        } else {
            // Android 9 (Pie) and below
            val targetDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "ExpenseTracker"
            ).apply { if (!exists()) mkdirs() }
            val destFile = File(targetDir, fileName)
            pdfFile.copyTo(destFile, overwrite = true)
            MediaScannerConnection.scanFile(context, arrayOf(destFile.absolutePath), arrayOf("application/pdf"), null)
            val uri = Uri.fromFile(destFile)
            return SavedReportResult(
                uri = uri,
                displayPath = "Phone Storage/Download/ExpenseTracker/$fileName",
                isSdCard = false
            )
        }
    }

    fun openPdf(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Open Statement").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
