package cut.the.crap.qreverywhere.compose.screens

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
import cut.the.crap.qreverywhere.compose.navigation.ComposeScreen
import cut.the.crap.qreverywhere.utils.ui.FROM_CREATE_CONTEXT
import cut.the.crap.qrrepository.Acquire
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Screen for creating QR codes from text/URL input
 * Supports: Plain Text, URLs, Phone numbers, SMS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeCreateTextQrScreen(
    navController: NavController,
    viewModel: MainActivityViewModel,
    qrType: String = "text" // text, url, phone, sms
) {
    var inputText by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Validation function
    fun validateInput(text: String, type: String): String? {
        if (text.isBlank()) {
            return when (type) {
                "url" -> "Please enter a valid URL"
                "phone" -> "Please enter a valid phone number"
                "sms" -> "Please enter a phone number"
                else -> "Please enter some text"
            }
        }

        return when (type) {
            "url" -> {
                val trimmedText = text.trim()
                if (!Patterns.WEB_URL.matcher(trimmedText).matches()) {
                    "Invalid URL format"
                } else null
            }
            "phone" -> {
                if (!Patterns.PHONE.matcher(text).matches()) {
                    "Invalid phone number"
                } else null
            }
            else -> null // Text and SMS don't need format validation beyond blank check
        }
    }

    // Determine labels and hints based on QR type
    val (title, label, hint, prefix) = when (qrType.lowercase()) {
        "url" -> {
            Tuple4("Create URL QR Code", "Website URL", "https://example.com", "")
        }
        "phone" -> {
            Tuple4("Create Phone QR Code", "Phone Number", "+1234567890", "tel:")
        }
        "sms" -> {
            Tuple4("Create SMS QR Code", "Phone Number", "+1234567890", "sms:")
        }
        else -> {
            Tuple4("Create Text QR Code", "Text Content", "Enter any text", "")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                text = "Enter the $label to encode in the QR code",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = inputText,
                onValueChange = {
                    inputText = it
                    // Clear error when user types
                    errorMessage = null
                },
                label = { Text(label) },
                placeholder = { Text(hint) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = qrType != "text",
                maxLines = if (qrType == "text") 5 else 1,
                isError = errorMessage != null,
                supportingText = errorMessage?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = when (qrType.lowercase()) {
                        "url" -> KeyboardType.Uri
                        "phone", "sms" -> KeyboardType.Phone
                        else -> KeyboardType.Text
                    },
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Button(
                onClick = {
                    // Validate input
                    val validationError = validateInput(inputText, qrType)
                    if (validationError != null) {
                        errorMessage = validationError
                        return@Button
                    }

                    isCreating = true
                    try {
                        // Add prefix if needed
                        val finalText = if (prefix.isNotEmpty()) {
                            "$prefix$inputText"
                        } else {
                            // For URLs, trim whitespace
                            if (qrType == "url") inputText.trim() else inputText
                        }

                        // Create QR code using ViewModel
                        viewModel.saveQrItemFromFile(finalText, Acquire.CREATED)

                        Timber.d("Created QR code with text: $finalText")

                        // Navigate to detail view
                        navController.navigate(ComposeScreen.DetailView.createRoute(FROM_CREATE_CONTEXT)) {
                            popUpTo(ComposeScreen.Create.route)
                        }
                    } catch (e: WriterException) {
                        Timber.e(e, "Error creating QR code")
                        scope.launch {
                            snackbarHostState.showSnackbar("Error creating QR code: ${e.message}")
                        }
                    } finally {
                        isCreating = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCreating
            ) {
                Text(if (isCreating) "Creating..." else "Create QR Code")
            }
        }
    }
}

// Helper data class for tuple
private data class Tuple4(
    val title: String,
    val label: String,
    val hint: String,
    val prefix: String
)
