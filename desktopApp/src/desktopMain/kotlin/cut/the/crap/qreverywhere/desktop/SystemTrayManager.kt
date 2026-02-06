package cut.the.crap.qreverywhere.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.rememberTrayState
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

/**
 * System Tray integration for macOS/Windows/Linux desktop
 *
 * Features:
 * - Show/hide main window
 * - Quick access to recent QR codes
 * - Generate QR from clipboard
 */
class SystemTrayManager(
    private val repository: QrRepository,
    private val qrGenerator: QrCodeGenerator,
    private val onShowWindow: () -> Unit,
    private val onHideWindow: () -> Unit,
    private val onCreateFromClipboard: () -> Unit,
    private val onOpenQrCode: (Int) -> Unit,
    private val onExit: () -> Unit
) {
    private var trayIcon: TrayIcon? = null
    private var isInitialized = false

    /**
     * Initialize the system tray icon if supported
     */
    fun initialize() {
        if (!SystemTray.isSupported()) {
            println("System tray is not supported on this platform")
            return
        }

        if (isInitialized) return

        try {
            val tray = SystemTray.getSystemTray()

            // Load tray icon (use a bundled icon)
            val iconStream = javaClass.getResourceAsStream("/tray_icon.png")
            val image = if (iconStream != null) {
                ImageIO.read(iconStream)
            } else {
                // Fallback: create a simple colored square
                java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB).apply {
                    val g = createGraphics()
                    g.color = java.awt.Color(76, 175, 80) // Material Green
                    g.fillRect(0, 0, 16, 16)
                    g.dispose()
                }
            }

            val popup = createPopupMenu()

            trayIcon = TrayIcon(image, "QR Everywhere", popup).apply {
                isImageAutoSize = true

                // Double-click to show window
                addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        if (e.clickCount == 2) {
                            onShowWindow()
                        }
                    }
                })
            }

            tray.add(trayIcon)
            isInitialized = true

        } catch (e: Exception) {
            println("Failed to initialize system tray: ${e.message}")
        }
    }

    /**
     * Create the popup menu for the tray icon
     */
    private fun createPopupMenu(): PopupMenu {
        return PopupMenu().apply {
            // Show Window
            add(MenuItem("Show QR Everywhere").apply {
                addActionListener { onShowWindow() }
            })

            addSeparator()

            // Quick Actions
            add(MenuItem("Create QR from Clipboard").apply {
                addActionListener { onCreateFromClipboard() }
            })

            add(MenuItem("Scan QR Code").apply {
                addActionListener {
                    onShowWindow()
                    // Note: The window needs to be shown first, then navigate to scan
                }
            })

            addSeparator()

            // Exit
            add(MenuItem("Quit").apply {
                addActionListener { onExit() }
            })
        }
    }

    /**
     * Update the popup menu with recent QR codes
     */
    fun updateRecentQrCodes(recentQrs: List<Pair<Int, String>>) {
        trayIcon?.let { icon ->
            val popup = createPopupMenu()

            // Insert recent QR codes before the separator
            if (recentQrs.isNotEmpty()) {
                val recentMenu = PopupMenu("Recent QR Codes")
                recentQrs.take(5).forEach { (id, content) ->
                    val truncatedContent = content.take(30) + if (content.length > 30) "..." else ""
                    recentMenu.add(MenuItem(truncatedContent).apply {
                        addActionListener {
                            onShowWindow()
                            onOpenQrCode(id)
                        }
                    })
                }

                // Insert at position 2 (after "Show" and first separator)
                popup.insert(recentMenu, 2)
            }

            icon.popupMenu = popup
        }
    }

    /**
     * Show a notification from the tray
     */
    fun showNotification(title: String, message: String) {
        trayIcon?.displayMessage(title, message, TrayIcon.MessageType.INFO)
    }

    /**
     * Remove the tray icon
     */
    fun dispose() {
        trayIcon?.let { icon ->
            if (SystemTray.isSupported()) {
                SystemTray.getSystemTray().remove(icon)
            }
        }
        trayIcon = null
        isInitialized = false
    }
}

/**
 * Compose-friendly System Tray component
 */
@Composable
fun ApplicationScope.QrSystemTray(
    repository: QrRepository,
    isWindowVisible: Boolean,
    onShowWindow: () -> Unit,
    onHideWindow: () -> Unit,
    onCreateFromClipboard: () -> Unit,
    onOpenQrCode: (Int) -> Unit
) {
    val trayState = rememberTrayState()
    val historyItems by repository.getQrHistory().collectAsState(initial = emptyList())

    // Use a generated icon since we don't have a PNG resource
    val trayIcon = remember {
        createTrayIconPainter()
    }

    Tray(
        state = trayState,
        icon = trayIcon,
        tooltip = "QR Everywhere",
        menu = {
            Item(
                text = if (isWindowVisible) "Hide Window" else "Show Window",
                onClick = {
                    if (isWindowVisible) onHideWindow() else onShowWindow()
                }
            )

            Separator()

            Item(
                text = "Create QR from Clipboard",
                onClick = {
                    onShowWindow()
                    onCreateFromClipboard()
                }
            )

            if (historyItems.isNotEmpty()) {
                Separator()

                Menu(text = "Recent QR Codes") {
                    historyItems.take(5).forEach { qrItem ->
                        val label = qrItem.textContent.take(25) +
                            if (qrItem.textContent.length > 25) "..." else ""
                        Item(
                            text = label,
                            onClick = {
                                onShowWindow()
                                onOpenQrCode(qrItem.id)
                            }
                        )
                    }
                }
            }

            Separator()

            Item(
                text = "Quit",
                onClick = ::exitApplication
            )
        }
    )
}

/**
 * Create a simple QR code icon for the system tray
 */
private fun createTrayIconPainter(): Painter {
    val size = 32
    val bitmap = Bitmap()
    val info = ImageInfo.makeN32(size, size, ColorAlphaType.PREMUL)
    bitmap.allocPixels(info)

    // Create a simple QR-like pattern
    val green = 0xFF4CAF50.toInt() // Material Green
    val white = 0xFFFFFFFF.toInt()

    // Draw a simple QR code pattern
    val moduleSize = size / 8
    val pattern = arrayOf(
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 0),
        intArrayOf(1, 0, 0, 0, 0, 0, 1, 0),
        intArrayOf(1, 0, 1, 1, 1, 0, 1, 0),
        intArrayOf(1, 0, 1, 1, 1, 0, 1, 0),
        intArrayOf(1, 0, 1, 1, 1, 0, 1, 0),
        intArrayOf(1, 0, 0, 0, 0, 0, 1, 0),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 0),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
    )

    // Convert IntArray to ByteArray (RGBA format)
    val pixels = ByteArray(size * size * 4)
    var byteIndex = 0
    for (y in 0 until size) {
        for (x in 0 until size) {
            val patternRow = y / moduleSize
            val patternCol = x / moduleSize
            val isGreen = patternRow < 8 && patternCol < 8 && pattern[patternRow][patternCol] == 1
            val color = if (isGreen) green else white

            // RGBA format
            pixels[byteIndex++] = ((color shr 16) and 0xFF).toByte() // R
            pixels[byteIndex++] = ((color shr 8) and 0xFF).toByte()  // G
            pixels[byteIndex++] = (color and 0xFF).toByte()          // B
            pixels[byteIndex++] = ((color shr 24) and 0xFF).toByte() // A
        }
    }

    bitmap.installPixels(info, pixels, size * 4)
    return BitmapPainter(bitmap.asComposeImageBitmap())
}
