package cut.the.crap.qreverywhere.shared.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * CameraX image analyzer for QR code detection
 */
internal class QrCodeAnalyzer(
    private val qrCodeDetector: QrCodeDetector,
    private val onQrCodeDetected: (QrCodeResult) -> Unit,
    private val onError: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var lastDetectedCode: String? = null
    private var lastDetectionTime: Long = 0
    private val detectionThrottleMs = 2000L // Prevent duplicate detections

    override fun analyze(image: ImageProxy) {
        val currentTime = Clock.System.now().toEpochMilliseconds()

        // Throttle detection to avoid processing same QR code multiple times
        if (currentTime - lastDetectionTime < detectionThrottleMs) {
            image.close()
            return
        }

        scope.launch {
            try {
                val results = qrCodeDetector.detectQrCodes(image)

                results.forEach { result ->
                    // Only emit if it's a different QR code or enough time has passed
                    if (result.text != lastDetectedCode ||
                        currentTime - lastDetectionTime > detectionThrottleMs) {
                        lastDetectedCode = result.text
                        lastDetectionTime = currentTime

                        // Switch to Main thread for UI updates
                        withContext(Dispatchers.Main) {
                            onQrCodeDetected(result)
                        }
                    }
                }
            } catch (e: Exception) {
                // Switch to Main thread for error callback
                withContext(Dispatchers.Main) {
                    onError("QR detection failed: ${e.message}")
                }
            } finally {
                image.close()
            }
        }
    }
}