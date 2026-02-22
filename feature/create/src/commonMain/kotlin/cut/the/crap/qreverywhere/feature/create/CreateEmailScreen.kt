package cut.the.crap.qreverywhere.feature.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*

@Composable
fun CreateEmailScreen(
    viewModel: CreateViewModel,
    onQrCreated: () -> Unit = {}
) {
    var emailAddress by remember { mutableStateOf("") }
    var emailSubject by remember { mutableStateOf("") }
    var emailBody by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    val errorEmptyEmail = stringResource(Res.string.error_empty_email)
    val errorInvalidEmail = stringResource(Res.string.error_invalid_email)

    fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".") && email.length > 5
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.instruction_email),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = emailAddress,
            onValueChange = {
                emailAddress = it
                emailError = null
            },
            label = { Text(stringResource(Res.string.label_email_address)) },
            placeholder = { Text(stringResource(Res.string.placeholder_email)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = emailSubject,
            onValueChange = { emailSubject = it },
            label = { Text(stringResource(Res.string.label_email_subject)) },
            placeholder = { Text(stringResource(Res.string.placeholder_email_subject)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = emailBody,
            onValueChange = { emailBody = it },
            label = { Text(stringResource(Res.string.label_email_message)) },
            placeholder = { Text(stringResource(Res.string.placeholder_email_message)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Button(
            onClick = {
                if (emailAddress.isBlank()) {
                    emailError = errorEmptyEmail
                    return@Button
                }

                if (!isValidEmail(emailAddress)) {
                    emailError = errorInvalidEmail
                    return@Button
                }

                isCreating = true

                val mailtoText = buildString {
                    append("mailto:")
                    append(emailAddress)
                    val params = mutableListOf<String>()
                    if (emailSubject.isNotBlank()) {
                        params.add("subject=${encodeUrlComponent(emailSubject)}")
                    }
                    if (emailBody.isNotBlank()) {
                        params.add("body=${encodeUrlComponent(emailBody)}")
                    }
                    if (params.isNotEmpty()) {
                        append("?")
                        append(params.joinToString("&"))
                    }
                }

                viewModel.createQrItem(mailtoText, AcquireType.CREATED) { result ->
                    isCreating = false
                    result.onSuccess { onQrCreated() }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCreating
        ) {
            Text(
                if (isCreating) stringResource(Res.string.create_button_creating)
                else stringResource(Res.string.create_button)
            )
        }
    }
}

private fun encodeUrlComponent(text: String): String {
    return text
        .replace("%", "%25")
        .replace(" ", "%20")
        .replace("&", "%26")
        .replace("=", "%3D")
        .replace("?", "%3F")
        .replace("#", "%23")
        .replace("\n", "%0A")
        .replace("\r", "%0D")
}
