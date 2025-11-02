package cut.the.crap.qreverywhere.shared.presentation

import androidx.compose.runtime.Composable
import java.util.Locale
import java.util.ResourceBundle

/**
 * Desktop implementation: Uses Java ResourceBundle
 *
 * This allows using Java's built-in localization system:
 * - messages.properties (default English)
 * - messages_es.properties (Spanish)
 * - messages_fr.properties (French)
 * - etc.
 *
 * For now, falls back to hardcoded English strings.
 * To enable desktop localization, add .properties files to resources.
 */
@Composable
actual fun getLocalizedStrings(): LocalizedStrings {
    // Option 1: Use ResourceBundle (commented out - requires .properties files)
    /*
    val bundle = try {
        ResourceBundle.getBundle("messages", Locale.getDefault())
    } catch (e: Exception) {
        null
    }

    return LocalizedStrings(
        appName = bundle?.getString("app_name") ?: "QR Everywhere",
        navHistory = bundle?.getString("nav_history") ?: "History",
        navScan = bundle?.getString("nav_scan") ?: "Scan",
        navCreate = bundle?.getString("nav_create") ?: "Create",
        // ... etc
    )
    */

    // Option 2: Fallback to English (used for now)
    return LocalizedStrings(
        appName = "QR Everywhere",
        navHistory = "History",
        navScan = "Scan",
        navCreate = "Create",
        titleHistory = "QR History",
        titleScan = "Scan QR Code",
        titleCreate = "Create QR Code",
        titleDetail = "QR Details",
        actionShare = "Share",
        actionSave = "Save Image",
        actionDelete = "Delete",
        actionCreate = "Generate QR Code",
        typeLabel = "Type",
        createdLabel = "Created",
        contentLabel = "Content",
        detailsLabel = "Details"
    )
}

/**
 * Helper function to get localized string from ResourceBundle
 */
fun getDesktopLocalizedString(key: String, defaultValue: String = ""): String {
    return try {
        ResourceBundle.getBundle("messages", Locale.getDefault()).getString(key)
    } catch (e: Exception) {
        defaultValue
    }
}
