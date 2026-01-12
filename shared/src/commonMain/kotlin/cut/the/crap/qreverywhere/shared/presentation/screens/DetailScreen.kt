package cut.the.crap.qreverywhere.shared.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cut.the.crap.qreverywhere.shared.presentation.state.State
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import cut.the.crap.qreverywhere.shared.utils.Logger
import cut.the.crap.qreverywhere.shared.utils.toImagePainter
import cut.the.crap.qreverywhere.shared.utils.toReadableString
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*

/**
 * Shared Detail Screen for Compose Multiplatform
 * Displays a single QR code with details
 *
 * Platform-specific features not yet implemented:
 * - Share functionality (needs platform-specific sharing APIs)
 * - Copy to clipboard (needs platform-specific clipboard APIs)
 * - Save to file (implemented via SaveImageToFileUseCase)
 */
@Composable
fun DetailScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit = {},
    onShare: () -> Unit = {},
    onCopyToClipboard: () -> Unit = {},
    onFullscreenClick: () -> Unit = {}
) {
    val detailItem by viewModel.detailViewItem.collectAsState()
    val detailState by viewModel.detailViewState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = detailState) {
            is State.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is State.Error -> {
                Text(
                    text = stringResource(Res.string.detail_error, state.message),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            is State.Success, null -> {
                detailItem?.let { qrItem ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // QR Code Image (clickable to view fullscreen)
                        qrItem.imageData?.let { imageBytes ->
                            imageBytes.toImagePainter()?.let { painter ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    onClick = onFullscreenClick
                                ) {
                                    Image(
                                        painter = painter,
                                        contentDescription = stringResource(Res.string.cd_qr_code),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .size(300.dp)
                                            .padding(16.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // QR Code Content
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = stringResource(Res.string.detail_content),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = qrItem.textContent,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Metadata
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(Res.string.detail_details),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                DetailRow(stringResource(Res.string.detail_type), qrItem.acquireType.name)
                                DetailRow(stringResource(Res.string.detail_created), qrItem.timestamp.toReadableString())
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Share Button
                            ExtendedFloatingActionButton(
                                onClick = {
                                    Logger.w("DetailScreen") { "Share not yet implemented for cross-platform" }
                                    onShare()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Share, stringResource(Res.string.cd_share))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(stringResource(Res.string.detail_share))
                            }

                            // Save Image Button
                            ExtendedFloatingActionButton(
                                onClick = {
                                    viewModel.saveQrImageOfDetailView()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Share, stringResource(Res.string.cd_save))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(stringResource(Res.string.detail_save))
                            }

                            // Delete Button
                            ExtendedFloatingActionButton(
                                onClick = {
                                    viewModel.deleteCurrentDetailView()
                                    onNavigateBack()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ) {
                                Icon(Icons.Default.Delete, stringResource(Res.string.cd_delete))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(stringResource(Res.string.detail_delete))
                            }
                        }
                    }
                } ?: run {
                    Text(
                        text = stringResource(Res.string.detail_no_item),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
