package cut.the.crap.qreverywhere.shared.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import cut.the.crap.qreverywhere.shared.utils.toImagePainter
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*

/**
 * Fullscreen Screen for Compose Multiplatform
 * Displays QR code in fullscreen with pinch-to-zoom capability
 *
 * Design adapted from Android ComposeFullscreenQrScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullscreenScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val detailItem by viewModel.detailViewItem.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = detailItem?.let {
                            // Format timestamp as subtitle
                            "${it.acquireType.name} • ${it.timestamp}"
                        } ?: "",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            detailItem?.imageData?.let { imageData ->
                imageData.toImagePainter()?.let { painter ->
                    ZoomableQrImage(painter = painter)
                }
            }
        }
    }
}

/**
 * Composable that displays an image with pinch-to-zoom and pan gestures
 */
@Composable
private fun ZoomableQrImage(painter: androidx.compose.ui.graphics.painter.Painter) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = stringResource(Res.string.cd_qr_fullscreen),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        // Update scale with limits (1x to 5x zoom)
                        scale = (scale * zoom).coerceIn(1f, 5f)

                        // Calculate max offset based on scale
                        val maxOffsetX = (size.width * (scale - 1)) / 2
                        val maxOffsetY = (size.height * (scale - 1)) / 2

                        // Update offset with pan gesture, constrained to bounds
                        offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                        offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)

                        // Reset offset when zoomed out to default
                        if (scale == 1f) {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
        )
    }
}
