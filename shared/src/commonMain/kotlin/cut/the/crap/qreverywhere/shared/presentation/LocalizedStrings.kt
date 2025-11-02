package cut.the.crap.qreverywhere.shared.presentation

import androidx.compose.runtime.Composable

/**
 * Platform-specific localized strings
 *
 * This allows each platform to provide its own localization:
 * - Android: Can use string resources from res/values/strings.xml
 * - iOS: Can use NSLocalizedString
 * - Desktop: Can use properties files or similar
 *
 * Usage:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val strings = getLocalizedStrings()
 *     Text(strings.navHistory)
 * }
 * ```
 */
data class LocalizedStrings(
    // App
    val appName: String,

    // Bottom Navigation
    val navHistory: String,
    val navScan: String,
    val navCreate: String,

    // Screen Titles
    val titleHistory: String,
    val titleScan: String,
    val titleCreate: String,
    val titleDetail: String,

    // Actions
    val actionShare: String,
    val actionSave: String,
    val actionDelete: String,
    val actionCreate: String,

    // Common
    val typeLabel: String,
    val createdLabel: String,
    val contentLabel: String,
    val detailsLabel: String,
)

/**
 * Get localized strings for the current platform/locale
 * Platform-specific implementation provides actual localization
 */
@Composable
expect fun getLocalizedStrings(): LocalizedStrings
