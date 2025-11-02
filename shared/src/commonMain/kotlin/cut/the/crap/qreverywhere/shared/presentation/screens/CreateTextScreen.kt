package cut.the.crap.qreverywhere.shared.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Card
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
 * Create Text QR Screen for Compose Multiplatform
 * Allows users to create QR codes from different text-based types
 *
 * @param qrType The type of QR code to create (text, url, phone, sms)
 * @param viewModel The shared ViewModel
 * @param onQrCreated Callback when QR code is created
 */
@Composable
fun CreateTextScreen(
    qrType: String = "text",
    viewModel: MainViewModel,
    onQrCreated: () -> Unit = {}
) {
    var textInput by remember { mutableStateOf("") }

    val (title, label, placeholder, prefix) = when (qrType) {
        "url" -> QrTypeInfo(
            "Web URL QR Code",
            "Enter website URL",
            "https://example.com",
            "https://"
        )
        "phone" -> QrTypeInfo(
            "Phone QR Code",
            "Enter phone number",
            "+1234567890",
            "tel:"
        )
        "sms" -> QrTypeInfo(
            "SMS QR Code",
            "Enter phone number and message",
            "+1234567890:Hello",
            "smsto:"
        )
        else -> QrTypeInfo(
            "Text QR Code",
            "Enter text",
            "Type anything...",
            ""
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
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
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text(label) },
                    placeholder = { Text(placeholder) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = if (qrType == "text") 3 else 1
                )

                if (qrType == "url" || qrType == "phone" || qrType == "sms") {
                    Text(
                        text = when (qrType) {
                            "url" -> "The URL should start with http:// or https://"
                            "phone" -> "Include country code for international numbers"
                            "sms" -> "Format: phone:message (e.g., +1234567890:Hello)"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            // Add prefix if needed (for phone, SMS, etc.)
                            val finalText = when {
                                qrType == "phone" && !textInput.startsWith("tel:") -> "tel:$textInput"
                                qrType == "sms" && !textInput.startsWith("smsto:") -> "smsto:$textInput"
                                qrType == "url" && !textInput.startsWith("http") -> "https://$textInput"
                                else -> textInput
                            }

                            viewModel.saveQrItemFromText(
                                textContent = finalText,
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
    }
}

private data class QrTypeInfo(
    val title: String,
    val label: String,
    val placeholder: String,
    val prefix: String
)
