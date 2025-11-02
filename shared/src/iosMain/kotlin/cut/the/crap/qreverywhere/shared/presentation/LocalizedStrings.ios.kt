package cut.the.crap.qreverywhere.shared.presentation

import androidx.compose.runtime.Composable
import platform.Foundation.NSBundle
import platform.Foundation.NSLocalizedString

/**
 * iOS implementation: Uses NSLocalizedString
 *
 * This allows using iOS's built-in localization system:
 * - Localizable.strings (default English)
 * - Localizable.strings (Spanish) in es.lproj
 * - Localizable.strings (French) in fr.lproj
 * - etc.
 *
 * For now, falls back to hardcoded English strings.
 * To enable iOS localization, add Localizable.strings files to your iOS app.
 */
@Composable
actual fun getLocalizedStrings(): LocalizedStrings {
    // Option 1: Use iOS NSLocalizedString (commented out - requires Localizable.strings)
    /*
    return LocalizedStrings(
        appName = NSLocalizedString("app_name", "QR Everywhere"),
        navHistory = NSLocalizedString("nav_history", "History"),
        navScan = NSLocalizedString("nav_scan", "Scan"),
        navCreate = NSLocalizedString("nav_create", "Create"),
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
 * Helper function to get localized string from iOS bundle
 */
fun getIosLocalizedString(key: String, defaultValue: String = ""): String {
    return NSLocalizedString(key, defaultValue)
}
