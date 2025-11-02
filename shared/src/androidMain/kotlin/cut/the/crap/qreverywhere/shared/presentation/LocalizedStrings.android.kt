package cut.the.crap.qreverywhere.shared.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Android implementation: Uses Android string resources
 *
 * This allows using Android's built-in localization system:
 * - res/values/strings.xml (default English)
 * - res/values-es/strings.xml (Spanish)
 * - res/values-fr/strings.xml (French)
 * - etc.
 *
 * For now, falls back to hardcoded English strings.
 * To enable Android resources, add string resources to your Android app.
 */
@Composable
actual fun getLocalizedStrings(): LocalizedStrings {
    val context = LocalContext.current

    // Option 1: Use Android resources (commented out - requires string resources to be defined)
    /*
    return LocalizedStrings(
        appName = context.getString(R.string.app_name),
        navHistory = context.getString(R.string.nav_history),
        navScan = context.getString(R.string.nav_scan),
        navCreate = context.getString(R.string.nav_create),
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
 * Alternative: Direct access to Android string resources
 * This can be used if you want to bypass the expect/actual pattern
 */
@Composable
fun getAndroidString(resourceId: Int): String {
    return LocalContext.current.getString(resourceId)
}
