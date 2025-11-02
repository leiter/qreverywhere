package cut.the.crap.qreverywhere.compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.compose.navigation.ComposeScreen
import cut.the.crap.qreverywhere.ui.theme.QrEveryWhereTheme
import cut.the.crap.qrrepository.QrItem
import org.koin.androidx.compose.koinViewModel

/**
 * Compose version of QrHistoryFragment
 * Displays list of scanned and created QR codes
 */
@Composable
fun ComposeHistoryScreen(
    navController: NavController,
    viewModel: MainActivityViewModel
) {
    val historyData by viewModel.historyData.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "QR History",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (historyData.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No QR codes yet.\nScan or create one to get started!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = historyData,
                    key = { it.id }
                ) { qrItem ->
                    QrHistoryItem(
                        qrItem = qrItem,
                        onClick = {
                            viewModel.setDetailViewItem(qrItem)
                            navController.navigate(ComposeScreen.DetailView.createRoute(2)) // FROM_HISTORY_LIST
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QrHistoryItem(
    qrItem: QrItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // QR Code thumbnail
            Image(
                bitmap = qrItem.img.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier.size(60.dp)
            )

            // QR Code info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = qrItem.textContent.take(50) + if (qrItem.textContent.length > 50) "..." else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Text(
                    text = "Type: ${when (qrItem.acquireType) {
                        0 -> "Scanned"
                        1 -> "Created"
                        2 -> "From File"
                        else -> "Unknown"
                    }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Preview removed - requires ViewModel parameter
