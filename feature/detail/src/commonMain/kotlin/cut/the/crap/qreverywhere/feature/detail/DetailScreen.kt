package cut.the.crap.qreverywhere.feature.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cut.the.crap.qreverywhere.shared.presentation.state.State
import cut.the.crap.qreverywhere.shared.utils.toImagePainter
import cut.the.crap.qreverywhere.shared.utils.toReadableString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onNavigateBack: () -> Unit = {},
    onShare: () -> Unit = {},
    onCopyToClipboard: () -> Unit = {},
    onFullscreenClick: () -> Unit = {}
) {
    val detailItem by viewModel.detailViewItem.collectAsState()
    val detailState by viewModel.detailViewState.collectAsState()
    val lastDeletedItem by viewModel.lastDeletedItem.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val deletedMessage = stringResource(Res.string.detail_deleted)
    val undoLabel = stringResource(Res.string.action_undo)

    // Show undo snackbar when an item is deleted
    LaunchedEffect(lastDeletedItem) {
        if (lastDeletedItem != null) {
            val result = snackbarHostState.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearLastDeletedItem()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
                        // QR preview animation
                        var animationStarted by remember { mutableStateOf(false) }
                        val scale by animateFloatAsState(
                            targetValue = if (animationStarted) 1f else 0.8f,
                            animationSpec = tween(durationMillis = 400)
                        )
                        val alpha by animateFloatAsState(
                            targetValue = if (animationStarted) 1f else 0f,
                            animationSpec = tween(durationMillis = 400)
                        )
                        LaunchedEffect(qrItem.id) { animationStarted = true }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            qrItem.imageData?.let { imageBytes ->
                                imageBytes.toImagePainter()?.let { painter ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp)
                                            .scale(scale)
                                            .alpha(alpha),
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

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ExtendedFloatingActionButton(
                                    onClick = onShare,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Share, stringResource(Res.string.cd_share))
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(stringResource(Res.string.detail_share))
                                }

                                ExtendedFloatingActionButton(
                                    onClick = {
                                        viewModel.saveQrImageOfDetailView()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_save),
                                        contentDescription = stringResource(Res.string.cd_save)
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(stringResource(Res.string.detail_save))
                                }

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
