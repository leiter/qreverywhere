package cut.the.crap.qreverywhere.shared.presentation

/**
 * String resources for Compose Multiplatform
 *
 * This provides a centralized place for all UI strings.
 * Can be extended with expect/actual for platform-specific localization.
 */
object Strings {
    // App Title
    const val appName = "QR Everywhere"

    // Bottom Navigation
    const val navHistory = "History"
    const val navScan = "Scan"
    const val navCreate = "Create"

    // Screen Titles
    const val titleHistory = "QR History"
    const val titleScan = "Scan QR Code"
    const val titleCreate = "Create QR Code"
    const val titleDetail = "QR Details"
    const val titleCreateText = "Create Text QR"
    const val titleCreateEmail = "Create Email QR"

    // History Screen
    const val historyEmpty = "No QR codes yet.\nScan or create one to get started!"
    const val historyType = "Type: %s"

    // Detail Screen
    const val detailContent = "Content"
    const val detailDetails = "Details"
    const val detailType = "Type"
    const val detailCreated = "Created"
    const val detailShare = "Share"
    const val detailSave = "Save Image"
    const val detailDelete = "Delete"
    const val detailNoItem = "No QR code selected"
    const val detailError = "Error: %s"

    // Create Screen
    const val createTitle = "Create QR Code"
    const val createQuickText = "Quick Text QR"
    const val createEnterText = "Enter text"
    const val createPlaceholder = "Type anything..."
    const val createButton = "Generate QR Code"
    const val createMoreTypes = "More QR Types"
    const val createComingSoon = "Coming soon:"
    const val createTypesList = "• Email QR Code\n• Phone QR Code\n• WiFi QR Code\n• vCard QR Code\n• URL QR Code"

    // Scan Screen
    const val scanTitle = "Camera Scanning"
    const val scanMessage = "QR code scanning requires platform-specific camera APIs.\n\n" +
            "This feature will be implemented using expect/actual declarations:\n" +
            "• Android: CameraX\n" +
            "• iOS: AVFoundation\n" +
            "• Desktop: Webcam libraries"

    // Content Descriptions (for accessibility)
    const val cdQrPreview = "QR Code Preview"
    const val cdQrCode = "QR Code"
    const val cdBack = "Back"
    const val cdMore = "More"
    const val cdShare = "Share"
    const val cdSave = "Save"
    const val cdDelete = "Delete"
    const val cdCreate = "Create"
}

/**
 * Extension function to format strings with parameters
 */
fun String.format(vararg args: Any?): String {
    var result = this
    args.forEachIndexed { index, arg ->
        result = result.replace("%s", arg.toString(), ignoreCase = false)
            .replace("%${index + 1}\$s", arg.toString(), ignoreCase = false)
    }
    return result
}
