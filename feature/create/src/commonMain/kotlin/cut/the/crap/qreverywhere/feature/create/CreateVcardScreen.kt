package cut.the.crap.qreverywhere.feature.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
fun CreateVcardScreen(
    viewModel: CreateViewModel,
    onQrCreated: () -> Unit = {}
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    val errorEmptyName = stringResource(Res.string.error_empty_name)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.instruction_contact),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = {
                firstName = it
                nameError = null
            },
            label = { Text(stringResource(Res.string.label_first_name)) },
            placeholder = { Text(stringResource(Res.string.placeholder_first_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = {
                lastName = it
                nameError = null
            },
            label = { Text(stringResource(Res.string.label_last_name)) },
            placeholder = { Text(stringResource(Res.string.placeholder_last_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = nameError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(Res.string.label_phone_number) + stringResource(Res.string.label_optional_suffix)) },
            placeholder = { Text(stringResource(Res.string.placeholder_phone)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(Res.string.label_email_address) + stringResource(Res.string.label_optional_suffix)) },
            placeholder = { Text(stringResource(Res.string.placeholder_email)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = organization,
            onValueChange = { organization = it },
            label = { Text(stringResource(Res.string.label_organization)) },
            placeholder = { Text(stringResource(Res.string.placeholder_organization)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = jobTitle,
            onValueChange = { jobTitle = it },
            label = { Text(stringResource(Res.string.label_job_title)) },
            placeholder = { Text(stringResource(Res.string.placeholder_job_title)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = website,
            onValueChange = { website = it },
            label = { Text(stringResource(Res.string.label_website)) },
            placeholder = { Text(stringResource(Res.string.placeholder_website)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Button(
            onClick = {
                if (firstName.isBlank() && lastName.isBlank()) {
                    nameError = errorEmptyName
                    return@Button
                }

                isCreating = true

                val vcardText = buildVcard(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    organization = organization.trim(),
                    jobTitle = jobTitle.trim(),
                    website = website.trim()
                )

                viewModel.createQrItem(vcardText, AcquireType.CREATED) { result ->
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

private fun buildVcard(
    firstName: String,
    lastName: String,
    phone: String,
    email: String,
    organization: String,
    jobTitle: String,
    website: String
): String {
    return buildString {
        appendLine("BEGIN:VCARD")
        appendLine("VERSION:3.0")
        appendLine("N:$lastName;$firstName;;;")

        val formattedName = listOfNotNull(
            firstName.takeIf { it.isNotEmpty() },
            lastName.takeIf { it.isNotEmpty() }
        ).joinToString(" ")
        appendLine("FN:$formattedName")

        if (phone.isNotEmpty()) {
            appendLine("TEL:$phone")
        }
        if (email.isNotEmpty()) {
            appendLine("EMAIL:$email")
        }
        if (organization.isNotEmpty()) {
            appendLine("ORG:$organization")
        }
        if (jobTitle.isNotEmpty()) {
            appendLine("TITLE:$jobTitle")
        }
        if (website.isNotEmpty()) {
            appendLine("URL:$website")
        }

        append("END:VCARD")
    }
}
