package cut.the.crap.qreverywhere.shared.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

/**
 * Cross-platform image resource handling
 *
 * Usage in Compose:
 * ```
 * Image(
 *     painter = AppIcons.QrCode.toPainter(),
 *     contentDescription = "QR Code"
 * )
 * ```
 */
object AppIcons {
    val QrCode = ImageResource("qr_code")
    val Scanner = ImageResource("ic_scanner")
    val History = ImageResource("ic_history")
    val Create = ImageResource("ic_create")
}

/**
 * Represents an image resource that can be loaded on any platform
 */
data class ImageResource(val name: String)

/**
 * Convert ImageResource to Compose Painter
 * Platform-specific implementation required
 */
@Composable
expect fun ImageResource.toPainter(): Painter

/**
 * Load image from ByteArray (for QR codes generated at runtime)
 * Platform-specific implementation required
 */
@Composable
expect fun ByteArray.toImagePainter(): Painter?
