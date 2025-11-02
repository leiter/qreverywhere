package cut.the.crap.qreverywhere.shared.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel

/**
 * Create Screen for Compose Multiplatform
 * Allows users to create QR codes from text
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    viewModel: MainViewModel,
    onQrCreated: () -> Unit = {}
) {
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Create QR Code",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Text QR Code Creation
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quick Text QR",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Enter text") },
                    placeholder = { Text("Type anything...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.saveQrItemFromText(
                                textContent = textInput,
                                acquireType = AcquireType.CREATED
                            )
                            textInput = ""
                            onQrCreated()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = textInput.isNotBlank()
                ) {
                    Icon(Icons.Default.Create, "Create")
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Generate QR Code")
                }
            }
        }

        // Additional QR Types (Placeholder)
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "More QR Types",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Coming soon:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "• Email QR Code\n• Phone QR Code\n• WiFi QR Code\n• vCard QR Code\n• URL QR Code",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
