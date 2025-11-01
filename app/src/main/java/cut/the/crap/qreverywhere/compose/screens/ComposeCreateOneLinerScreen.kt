package cut.the.crap.qreverywhere.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.compose.navigation.ComposeScreen
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.qrcodecreate.CreateOneLinerFragment
import cut.the.crap.qreverywhere.qrcodecreate.CreateOneLinerViewModel
import cut.the.crap.qreverywhere.ui.theme.QrEveryWhereTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Compose version of CreateOneLinerFragment
 * Screen for creating Phone, SMS, or Web URL QR codes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeCreateOneLinerScreen(
    navController: NavController,
    useCaseMode: Int,
    viewModel: CreateOneLinerViewModel = koinViewModel(),
    activityViewModel: MainActivityViewModel
) {
    var inputText by remember { mutableStateOf("") }
    var inputNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Determine screen details based on mode
    val (screenTitle, inputLabel, inputPlaceholder, keyboardType) = when (useCaseMode) {
        CreateOneLinerFragment.CREATE_PHONE -> {
            Quadruple("Phone QR Code", "Phone Number *", "+1234567890", KeyboardType.Phone)
        }
        CreateOneLinerFragment.CREATE_SMS -> {
            Quadruple("SMS QR Code", "Message *", "Enter your message", KeyboardType.Text)
        }
        CreateOneLinerFragment.CREATE_WEB -> {
            Quadruple("Web URL QR Code", "URL *", "https://example.com", KeyboardType.Uri)
        }
        else -> Quadruple("Create QR Code", "Input *", "Enter text", KeyboardType.Text)
    }

    // Observe ViewModel state
    val qrCodeState by viewModel.qrCodeItemState.observeAsState()

    LaunchedEffect(qrCodeState) {
        qrCodeState?.let { state ->
            when (state) {
                is State.Loading -> {
                    isLoading = true
                }
                is State.Success -> {
                    isLoading = false
                    state.data?.let { qrItem ->
                        // Test/preview mode - just show the QR
                        activityViewModel.setDetailViewItem(qrItem)
                    } ?: run {
                        // Create mode - navigate to detail
                        navController.navigate(ComposeScreen.DetailView.createRoute(1)) { // FROM_CREATE_CONTEXT
                            popUpTo(ComposeScreen.Create.route)
                        }
                    }
                }
                is State.Error -> {
                    isLoading = false
                    val errorMessage = when (state.cause) {
                        is cut.the.crap.qreverywhere.qrcodecreate.InvalidPhoneNumber -> "Invalid phone number"
                        is cut.the.crap.qreverywhere.qrcodecreate.InvalidWebUrl -> "Invalid URL"
                        is cut.the.crap.qreverywhere.qrcodecreate.EmptyMessage -> "Message cannot be empty"
                        else -> state.message ?: "Error creating QR code"
                    }
                    snackbarHostState.showSnackbar(errorMessage)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create a $screenTitle",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            val description = when (useCaseMode) {
                CreateOneLinerFragment.CREATE_PHONE -> "When scanned, this QR code will allow users to call this phone number"
                CreateOneLinerFragment.CREATE_SMS -> "When scanned, this QR code will open an SMS client with your message"
                CreateOneLinerFragment.CREATE_WEB -> "When scanned, this QR code will open this website in a browser"
                else -> "Enter the content for your QR code"
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Input Field (phone uses separate field for number, others use text)
            if (useCaseMode == CreateOneLinerFragment.CREATE_PHONE) {
                OutlinedTextField(
                    value = inputNumber,
                    onValueChange = { inputNumber = it },
                    label = { Text(inputLabel) },
                    placeholder = { Text(inputPlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    singleLine = true,
                    enabled = !isLoading
                )
            } else {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text(inputLabel) },
                    placeholder = { Text(inputPlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    minLines = if (useCaseMode == CreateOneLinerFragment.CREATE_SMS) 3 else 1,
                    maxLines = if (useCaseMode == CreateOneLinerFragment.CREATE_SMS) 5 else 1,
                    enabled = !isLoading
                )
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.currentInputText = inputText
                            viewModel.currentInputNumber = inputNumber
                            viewModel.testClicked(useCaseMode)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && (if (useCaseMode == CreateOneLinerFragment.CREATE_PHONE) inputNumber.isNotBlank() else inputText.isNotBlank())
                    ) {
                        Text("Test")
                    }

                    Button(
                        onClick = {
                            viewModel.currentInputText = inputText
                            viewModel.currentInputNumber = inputNumber
                            viewModel.createClicked(useCaseMode, activityViewModel)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && (if (useCaseMode == CreateOneLinerFragment.CREATE_PHONE) inputNumber.isNotBlank() else inputText.isNotBlank())
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Text("Create")
                    }
                }

                OutlinedButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

// Helper data class
private data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

// Preview removed - requires ViewModel parameter
