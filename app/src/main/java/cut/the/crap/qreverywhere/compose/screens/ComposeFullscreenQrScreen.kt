package cut.the.crap.qreverywhere.compose.screens

import android.graphics.Bitmap
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.detailViewQrCodeItem
import cut.the.crap.qreverywhere.detailViewQrCodeItemState
import cut.the.crap.qreverywhere.shared.presentation.state.getData
import cut.the.crap.qreverywhere.utils.data.AcquireDateFormatter
import cut.the.crap.qreverywhere.shared.presentation.OriginFlag
import cut.the.crap.qrrepository.QrItem
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*
import timber.log.Timber

/**
 * Compose version of QrFullscreenFragment
 * Displays QR code in fullscreen with pinch-to-zoom capability
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeFullscreenQrScreen(
    navController: NavController,
    originFlag: Int,
    viewModel: MainActivityViewModel,
    dateFormatter: AcquireDateFormatter = koinInject()
) {
    // For scanned items, collect StateFlow
    val scannedItemState by viewModel.detailViewQrCodeItemState.collectAsStateWithLifecycle()

    // For history/create items, use state to hold the captured item
    var capturedItem by remember { mutableStateOf<QrItem?>(null) }

    // Capture the item from ViewModel when screen loads
    LaunchedEffect(originFlag) {
        if (originFlag == OriginFlag.FROM_HISTORY_LIST || originFlag == OriginFlag.FROM_CREATE_CONTEXT) {
            var attempts = 0
            while (attempts < 20) {
                val item = viewModel.detailViewQrCodeItem
                if (item.id != 0 || item.img.width > 1) {
                    capturedItem = item
                    Timber.d("Captured VALID item from ViewModel: id=${item.id}, bitmap=${item.img.width}x${item.img.height}")
                    break
                } else {
                    Timber.w("Attempt $attempts: Item not ready yet, waiting...")
                    kotlinx.coroutines.delay(50)
                    attempts++
                }
            }
        }
    }

    // Determine which item to display based on origin
    val qrItem: QrItem? = when (originFlag) {
        OriginFlag.FROM_HISTORY_LIST, OriginFlag.FROM_CREATE_CONTEXT -> capturedItem
        OriginFlag.FROM_SCAN_QR -> scannedItemState?.getData()
        else -> null
    }

    val subtitle = qrItem?.let { dateFormatter.getTimeTemplate(it) } ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
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
            qrItem?.let { item ->
                ZoomableQrImage(bitmap = item.img)
            }
        }
    }
}

/**
 * Composable that displays a bitmap image with pinch-to-zoom and pan gestures
 */
@Composable
private fun ZoomableQrImage(bitmap: Bitmap) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
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
                        // Update scale with limits
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
