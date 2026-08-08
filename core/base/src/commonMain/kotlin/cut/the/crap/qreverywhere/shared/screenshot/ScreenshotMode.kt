package cut.the.crap.qreverywhere.shared.screenshot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Screenshot capture mode for App Store assets.
 *
 * Off unless the host app switches it on from a launch argument, so a normally
 * launched build never sees any of this. The capture script in
 * `iosApp/scripts/capture_screenshots.sh` drives it, which is why nothing here
 * needs taps: the app is told which screen to show and launches straight into it.
 *
 * Screens render demo content instead of whatever happens to be on the device,
 * so a locale sweep produces the same content in every language.
 */
object ScreenshotMode {

    /** Set from `-screenshotMode YES`. */
    var enabled: Boolean = false

    /**
     * QR image shown behind the scan overlay, since a simulator has no camera.
     * Populated by the seeder with a real generated QR code.
     *
     * Compose state, because the seeder fills it in asynchronously and the scan
     * screen is usually already on screen by then.
     */
    var scanPreviewImage: ByteArray? by mutableStateOf(null)

    /** Demo values for form screens, which would otherwise be captured empty. */
    const val DEMO_WIFI_SSID: String = "Aurora Cafe"
    const val DEMO_WIFI_PASSWORD: String = "roastedbeans"

    /** Content seeded into the history so list and detail screens look real. */
    val demoHistory: List<DemoEntry> = listOf(
        DemoEntry("https://qreverywhere.app", DemoKind.CREATED),
        DemoEntry("WIFI:T:WPA;S:$DEMO_WIFI_SSID;P:$DEMO_WIFI_PASSWORD;;", DemoKind.CREATED),
        DemoEntry(
            "BEGIN:VCARD\nVERSION:3.0\nN:Rivera;Jordan\nFN:Jordan Rivera\n" +
                "TEL:+1 555 0147\nEMAIL:jordan@example.com\nEND:VCARD",
            DemoKind.CREATED
        ),
        DemoEntry("geo:48.8584,2.2945", DemoKind.SCANNED),
        DemoEntry("mailto:hello@example.com", DemoKind.SCANNED),
        DemoEntry("tel:+15550147", DemoKind.CREATED)
    )

    data class DemoEntry(val content: String, val kind: DemoKind)

    enum class DemoKind { CREATED, SCANNED }
}
