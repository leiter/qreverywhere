package cut.the.crap.qreverywhere.feature.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*

@Composable
fun CreateTextScreen(
    qrType: String = "text",
    viewModel: CreateViewModel,
    onQrCreated: () -> Unit = {}
) {
    var textInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    val errorEmptyText = stringResource(Res.string.error_empty_text)
    val errorInvalidUrl = stringResource(Res.string.error_invalid_url)
    val errorInvalidPhone = stringResource(Res.string.error_invalid_phone)

    fun isValidUrl(url: String): Boolean {
        val urlPattern = url.lowercase()
        return urlPattern.contains(".") && urlPattern.length > 4
    }

    fun isValidPhone(phone: String): Boolean {
        val digitsOnly = phone.filter { it.isDigit() || it == '+' }
        return digitsOnly.length >= 6
    }

    val (title, label, placeholder, prefix) = when (qrType) {
        "url" -> QrTypeInfo(
            stringResource(Res.string.title_url_qr),
            stringResource(Res.string.label_enter_url),
            stringResource(Res.string.placeholder_url),
            "https://"
        )
        "phone" -> QrTypeInfo(
            stringResource(Res.string.title_phone_qr),
            stringResource(Res.string.label_enter_phone),
            stringResource(Res.string.placeholder_phone),
            "tel:"
        )
        "sms" -> QrTypeInfo(
            stringResource(Res.string.title_sms_qr),
            stringResource(Res.string.label_enter_phone_message),
            stringResource(Res.string.placeholder_sms),
            "smsto:"
        )
        else -> QrTypeInfo(
            stringResource(Res.string.title_text_qr),
            stringResource(Res.string.create_enter_text),
            stringResource(Res.string.create_placeholder),
            ""
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = {
                        textInput = it
                        inputError = null
                    },
                    label = { Text(label) },
                    placeholder = { Text(placeholder) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = if (qrType == "text") 3 else 1,
                    isError = inputError != null,
                    supportingText = inputError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = when (qrType) {
                            "url" -> KeyboardType.Uri
                            "phone" -> KeyboardType.Phone
                            else -> KeyboardType.Text
                        }
                    )
                )

                if (qrType == "url" || qrType == "phone" || qrType == "sms") {
                    Text(
                        text = when (qrType) {
                            "url" -> stringResource(Res.string.hint_url)
                            "phone" -> stringResource(Res.string.hint_phone)
                            "sms" -> stringResource(Res.string.hint_sms)
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = {
                        if (textInput.isBlank()) {
                            inputError = errorEmptyText
                            return@OutlinedButton
                        }

                        when (qrType) {
                            "url" -> {
                                if (!isValidUrl(textInput)) {
                                    inputError = errorInvalidUrl
                                    return@OutlinedButton
                                }
                            }
                            "phone" -> {
                                if (!isValidPhone(textInput)) {
                                    inputError = errorInvalidPhone
                                    return@OutlinedButton
                                }
                            }
                        }

                        val finalText = when {
                            qrType == "phone" && !textInput.startsWith("tel:") -> "tel:$textInput"
                            qrType == "sms" && !textInput.startsWith("smsto:") -> "smsto:$textInput"
                            qrType == "url" && !textInput.startsWith("http") -> "https://$textInput"
                            else -> textInput
                        }

                        viewModel.createQrItem(
                            textContent = finalText,
                            acquireType = AcquireType.CREATED
                        ) { result ->
                            result.onSuccess {
                                onQrCreated()
                            }
                        }
                        textInput = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = textInput.isNotBlank()
                ) {
                    Icon(Icons.Default.Create, stringResource(Res.string.cd_create))
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(stringResource(Res.string.create_button))
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
