package cut.the.crap.qreverywhere.shared.utils

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext

/**
 * Android implementation: Convert ImageResource to Painter
 * Uses reflection to get drawable resource ID from name
 */
@Composable
actual fun ImageResource.toPainter(): Painter {
    val context = LocalContext.current

    // Get drawable resource ID by name using reflection
    // This is how Android resolves "qr_code" -> R.drawable.qr_code (Int)
    val resourceId = context.resources.getIdentifier(
        name,
        "drawable",
        context.packageName
    )

    if (resourceId == 0) {
        Logger.w("ImageResource") { "Drawable resource not found: $name" }
        // Return a default painter or throw exception
        return BitmapPainter(
            android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888)
                .asImageBitmap()
        )
    }

    return androidx.compose.ui.res.painterResource(id = resourceId)
}

/**
 * Android implementation: Convert ByteArray to ImagePainter
 * Used for displaying generated QR codes
 */
@Composable
actual fun ByteArray.toImagePainter(): Painter? {
    if (isEmpty()) return null

    val bitmap = BitmapFactory.decodeByteArray(this, 0, size) ?: return null
    return BitmapPainter(bitmap.asImageBitmap())
}
