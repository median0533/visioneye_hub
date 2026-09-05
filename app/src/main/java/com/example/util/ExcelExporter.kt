package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.User
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility to generate and export clean, formatted Excel spreadsheet files.
 *
 * Solutions applied:
 * 1. Mobile Number formatting: Formatted as `="<phone>"` or tab-prefixed so Excel treats
 *    it strictly as TEXT, completely preventing scientific notation (9.18521E+11).
 * 2. Date formatting: Formatted with a clean, standard date format (yyyy-MM-dd HH:mm) as TEXT
 *    so Excel does NOT show column-width overflow errors (#######).
 * 3. File extension & MIME type: Writes an Excel-compatible spreadsheet with UTF-8 BOM,
 *    registering MIME types `application/vnd.ms-excel` and `text/csv` so Microsoft Excel
 *    and Google Sheets appear as primary choices in the app selector.
 */
object ExcelExporter {

    private const val TAG = "ExcelExporter"

    /**
     * Exports user records to an Excel-readable spreadsheet and launches Android's
     * system chooser with Microsoft Excel and Google Sheets prominently listed.
     *
     * The CSV file is named according to: vISIONeYe_Users_<timestamp>.csv
     */
    fun exportUsersToExcel(
        context: Context,
        users: List<User>,
        fileNamePrefix: String = "vISIONeYe_Users"
    ) {
        if (users.isEmpty()) {
            Toast.makeText(context, "No users available to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Save inside cache/exports directory
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            // File name is: vISIONeYe_Users_<timestamp>.csv
            val safePrefix = if (fileNamePrefix.isNotBlank()) sanitizeFileName(fileNamePrefix) else "vISIONeYe_Users"
            val fileName = "${safePrefix}_$timestamp.csv"
            val file = File(exportDir, fileName)

            // Standard concise date format that fits Excel cell widths cleanly
            val dateDisplayFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            FileOutputStream(file).use { fos ->
                // UTF-8 BOM (Byte Order Mark) ensures Microsoft Excel & Google Sheets parse UTF-8 characters correctly
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    // Excel Header Row (Only requested user fields)
                    val headers = listOf(
                        "Customer Name",
                        "Email Address",
                        "Mobile Number",
                        "Company",
                        "Address",
                        "City",
                        "State",
                        "Country",
                        "Pincode",
                        "Account Status",
                        "Registration Date",
                        "Updated Date",
                        "Last Login Date"
                    )
                    writer.write(headers.joinToString(separator = ",", postfix = "\r\n") { escapeCsv(it) })

                    // Data Rows
                    users.forEach { user ->
                        val createdAtStr = if (user.createdAt > 0) dateDisplayFormat.format(Date(user.createdAt)) else "Not Available"
                        val updatedAtStr = if (user.updatedAt > 0) dateDisplayFormat.format(Date(user.updatedAt)) else "Not Available"
                        val lastLoginStr = user.lastLoginAt?.takeIf { it > 0 }?.let { dateDisplayFormat.format(Date(it)) } ?: "Never"

                        val customerNameDisplay = when {
                            user.customerName.isNotBlank() -> user.customerName
                            user.businessName.isNotBlank() -> user.businessName
                            user.displayName.isNotBlank() -> user.displayName
                            else -> "Not Available"
                        }

                        val companyDisplay = when {
                            user.company.isNotBlank() -> user.company
                            user.businessName.isNotBlank() -> user.businessName
                            else -> "Not Available"
                        }

                        // Format phone number properly
                        val rawMobile = when {
                            user.fullMobileNumber.isNotBlank() && user.fullMobileNumber != "Not Available" -> user.fullMobileNumber
                            user.mobileNumber.isNotBlank() -> user.mobileNumber
                            else -> "Not Available"
                        }

                        val row = listOf(
                            escapeCsv(customerNameDisplay),
                            escapeCsv(user.email.ifBlank { "Not Available" }),
                            formatAsExcelText(rawMobile), // Formatted as text formula so Excel NEVER converts to 9.18E+11
                            escapeCsv(companyDisplay),
                            escapeCsv(user.address.ifBlank { "Not Available" }),
                            escapeCsv(user.city.ifBlank { "Not Available" }),
                            escapeCsv(user.state.ifBlank { "Not Available" }),
                            escapeCsv(user.country.ifBlank { "Not Available" }),
                            escapeCsv(user.pincode.ifBlank { "Not Available" }),
                            escapeCsv(user.userStatus.label),
                            formatAsExcelText(createdAtStr), // Formatted as text so Excel doesn't display ### on narrow width
                            formatAsExcelText(updatedAtStr),
                            formatAsExcelText(lastLoginStr)
                        )
                        writer.write(row.joinToString(separator = ",", postfix = "\r\n"))
                    }
                    writer.flush()
                }
            }

            // Generate content URI via FileProvider
            val authority = "${context.packageName}.fileprovider"
            val fileUri: Uri = FileProvider.getUriForFile(context, authority, file)

            // Launch custom or native chooser that specifically prompts for Microsoft Excel / Google Sheets
            launchSpreadsheetChooser(context, fileUri, safePrefix, fileName)
            Toast.makeText(context, "Exported: $fileName", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error exporting users to Excel", e)
            Toast.makeText(context, "Failed to export Excel: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Sanitizes a customer name to be a clean, valid file name component.
     * Replaces spaces with underscores and removes forbidden filesystem characters.
     */
    fun sanitizeFileName(name: String): String {
        val clean = name.trim()
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .replace(Regex("\\s+"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
        return if (clean.isNotBlank()) clean else "Customer"
    }

    /**
     * Formats a value as an Excel text formula: `="value"`
     * In CSV/Excel:
     * - A raw string like `+918530707040` gets converted to number 918530707040 and displayed as `9.18521E+11`.
     * - By formatting it as `="=""="""` or `="<value>"`, Excel treats it as an exact literal string formula.
     * This guarantees that phone numbers (+91...) and dates never get converted to scientific notation or `#######`.
     */
    private fun formatAsExcelText(value: String): String {
        if (value.isBlank() || value == "Not Available" || value == "Never") {
            return escapeCsv(value)
        }
        val cleanValue = value.replace("\"", "\"\"")
        return "\"=\"\"$cleanValue\"\"\""
    }

    /**
     * Escapes standard CSV values according to RFC 4180.
     */
    private fun escapeCsv(value: String?): String {
        if (value == null) return "\"\""
        val trimmed = value.trim()
        val escaped = trimmed.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    /**
     * Creates an Android Intent Chooser tailored to show Microsoft Excel, Google Sheets,
     * or any installed spreadsheet app.
     */
    private fun launchSpreadsheetChooser(
        context: Context,
        fileUri: Uri,
        customerName: String,
        fileName: String
    ) {
        val pm = context.packageManager

        // Intent for viewing spreadsheet
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/vnd.ms-excel")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Also create a CSV view intent
        val csvViewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "text/csv")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Send Intent for sharing via Drive / Sheets / Excel
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "$customerName Export ($fileName)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Collect matching intents
        val targetedIntents = mutableListOf<Intent>()

        // Look specifically for Microsoft Excel and Google Sheets packages
        val knownSpreadsheetPackages = listOf(
            "com.microsoft.office.excel",
            "com.google.android.apps.docs.editors.sheets",
            "com.microsoft.office.officehubrow",
            "cn.wps.moffice_eng"
        )

        for (pkg in knownSpreadsheetPackages) {
            try {
                pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)
                // If package exists on device, create explicit intent
                val explicitIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(fileUri, "text/csv")
                    setPackage(pkg)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                targetedIntents.add(explicitIntent)
            } catch (_: Exception) {
                // Not installed, ignore
            }
        }

        // Always add send intent and csvViewIntent
        targetedIntents.add(sendIntent)
        targetedIntents.add(csvViewIntent)

        val chooserTitle = "Open with Microsoft Excel / Google Sheets"
        val chooser = Intent.createChooser(viewIntent, chooserTitle).apply {
            if (targetedIntents.isNotEmpty()) {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedIntents.toTypedArray<Parcelable>())
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(chooser)
    }
}
