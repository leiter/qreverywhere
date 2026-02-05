package cut.the.crap.qreverywhere.shared.presentation.screens

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
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*

/**
 * Screen for creating QR codes with contact information (vCard 3.0 format)
 *
 * Output format:
 * BEGIN:VCARD
 * VERSION:3.0
 * N:LastName;FirstName;;;
 * FN:FirstName LastName
 * TEL:+1234567890
 * EMAIL:email@example.com
 * ORG:Company
 * TITLE:Job Title
 * URL:https://website.com
 * END:VCARD
 */
@Composable
fun CreateVcardScreen(
    viewModel: MainViewModel,
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

    // Get localized strings
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

        // First Name Field
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

        // Last Name Field
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

        // Phone Field
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(Res.string.label_phone_number) + " (Optional)") },
            placeholder = { Text(stringResource(Res.string.placeholder_phone)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            )
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(Res.string.label_email_address) + " (Optional)") },
            placeholder = { Text(stringResource(Res.string.placeholder_email)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        // Organization Field
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

        // Job Title Field
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

        // Website Field
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
                // Validate at least first or last name is provided
                if (firstName.isBlank() && lastName.isBlank()) {
                    nameError = errorEmptyName
                    return@Button
                }

                isCreating = true
                try {
                    // Build vCard 3.0 string
                    val vcardText = buildVcard(
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        phone = phone.trim(),
                        email = email.trim(),
                        organization = organization.trim(),
                        jobTitle = jobTitle.trim(),
                        website = website.trim()
                    )

                    // Create QR code using ViewModel
                    viewModel.saveQrItemFromText(vcardText, AcquireType.CREATED)

                    onQrCreated()
                } finally {
                    isCreating = false
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

/**
 * Build vCard 3.0 formatted string
 */
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

        // N (Name) - required: LastName;FirstName;MiddleName;Prefix;Suffix
        appendLine("N:$lastName;$firstName;;;")

        // FN (Formatted Name) - required
        val formattedName = listOfNotNull(
            firstName.takeIf { it.isNotEmpty() },
            lastName.takeIf { it.isNotEmpty() }
        ).joinToString(" ")
        appendLine("FN:$formattedName")

        // TEL (Phone) - optional
        if (phone.isNotEmpty()) {
            appendLine("TEL:$phone")
        }

        // EMAIL - optional
        if (email.isNotEmpty()) {
            appendLine("EMAIL:$email")
        }

        // ORG (Organization) - optional
        if (organization.isNotEmpty()) {
            appendLine("ORG:$organization")
        }

        // TITLE (Job Title) - optional
        if (jobTitle.isNotEmpty()) {
            appendLine("TITLE:$jobTitle")
        }

        // URL (Website) - optional
        if (website.isNotEmpty()) {
            appendLine("URL:$website")
        }

        append("END:VCARD")
    }
}
