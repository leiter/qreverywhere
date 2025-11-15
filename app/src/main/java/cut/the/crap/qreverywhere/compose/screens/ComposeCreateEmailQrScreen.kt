package cut.the.crap.qreverywhere.compose.screens

import android.net.Uri
import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.zxing.WriterException
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.saveQrItemFromFile
import cut.the.crap.qreverywhere.compose.navigation.ComposeScreen
import cut.the.crap.qreverywhere.utils.ui.FROM_CREATE_CONTEXT
import cut.the.crap.qrrepository.Acquire
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*
import timber.log.Timber

/**
 * Screen for creating QR codes with email information
 * Creates a mailto: URI with email, subject, and body
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeCreateEmailQrScreen(
    navController: NavController,
    viewModel: MainActivityViewModel
) {
    var emailAddress by remember { mutableStateOf("") }
    var emailSubject by remember { mutableStateOf("") }
    var emailBody by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Get localized strings
    val errorEmptyEmail = stringResource(Res.string.error_empty_email)
    val errorInvalidEmail = stringResource(Res.string.error_invalid_email)
    val errorCreateQr = stringResource(Res.string.error_create_qr)

    // Email validation function
    fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.create_email_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.cd_back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.instruction_email),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Email Address Field
            OutlinedTextField(
                value = emailAddress,
                onValueChange = {
                    emailAddress = it
                    // Clear error when user types
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
                ),
                keyboardActions = KeyboardActions(
                    onNext = { /* Focus moves automatically */ }
                )
            )

            // Email Subject Field
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

            // Email Body Field
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
                    // Validate email
                    if (emailAddress.isBlank()) {
                        emailError = errorEmptyEmail
                        return@Button
                    }

                    if (!validateEmail(emailAddress)) {
                        emailError = errorInvalidEmail
                        return@Button
                    }

                    isCreating = true
                    try {
                        // Create mailto URI
                        val mailtoText = "mailto:%s?subject=%s&body=%s".format(
                            Uri.encode(emailAddress),
                            Uri.encode(emailSubject),
                            Uri.encode(emailBody)
                        )

                        // Create QR code using ViewModel
                        viewModel.saveQrItemFromFile(mailtoText, Acquire.CREATED)

                        Timber.d("Created email QR code: $mailtoText")

                        // Navigate to detail view
                        navController.navigate(ComposeScreen.DetailView.createRoute(FROM_CREATE_CONTEXT)) {
                            popUpTo(ComposeScreen.Create.route)
                        }
                    } catch (e: WriterException) {
                        Timber.e(e, "Error creating QR code")
                        scope.launch {
                            snackbarHostState.showSnackbar(errorCreateQr.replace("%1\$s", e.message ?: ""))
                        }
                    } finally {
                        isCreating = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCreating
            ) {
                Text(if (isCreating) stringResource(Res.string.create_button_creating) else stringResource(Res.string.create_button))
            }
        }
    }
}
