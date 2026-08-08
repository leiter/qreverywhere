package cut.the.crap.qreverywhere.shared.screenshot

import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import cut.the.crap.qreverywhere.shared.domain.model.QrItem
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Replaces the on-device history with [ScreenshotMode.demoHistory] so every
 * locale in a capture run shows identical content.
 *
 * Destructive by design — only ever called when screenshot mode is on.
 */
class ScreenshotSeeder(
    private val repository: QrRepository,
    private val generator: QrCodeGenerator
) {

    /**
     * Seeds the demo history and returns the item to show on the detail screen.
     *
     * Timestamps are spread backwards from now so the history list shows a
     * plausible spread of dates rather than six identical ones.
     */
    suspend fun seed(): QrItem? {
        repository.deleteAll()

        val now = Clock.System.now()
        var newest: QrItem? = null

        ScreenshotMode.demoHistory.forEachIndexed { index, entry ->
            val item = QrItem(
                id = 0,
                textContent = entry.content,
                acquireType = when (entry.kind) {
                    ScreenshotMode.DemoKind.CREATED -> AcquireType.CREATED
                    ScreenshotMode.DemoKind.SCANNED -> AcquireType.SCANNED
                },
                timestamp = now - (index * 19).hours,
                imageData = generator.generateQrCode(entry.content)
            )
            repository.insertQrItem(item)
            if (newest == null) newest = item
        }

        // The scan screen has no camera on a simulator, so it frames this
        // instead — a real QR code, rendered by the app's own generator.
        ScreenshotMode.scanPreviewImage = generator.generateQrCode("https://qreverywhere.app")

        return newest
    }
}
