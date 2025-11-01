package cut.the.crap.qreverywhere.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.compose.navigation.ComposeScreen
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.qrcodecreate.CreateQrCodeViewModel
import cut.the.crap.qreverywhere.ui.theme.QrEveryWhereTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Compose version of CreateEmailQrCodeFragment
 * Screen for creating Email QR codes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeCreateEmailScreen(
    navController: NavController,
    viewModel: CreateQrCodeViewModel = koinViewModel(),
    activityViewModel: MainActivityViewModel
) {
    var emailAddress by remember { mutableStateOf("") }
    var emailSubject by remember { mutableStateOf("") }
    var emailText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Observe ViewModel state
    val emailQrState by viewModel.emailQrCodeItem.observeAsState()

    LaunchedEffect(emailQrState) {
        emailQrState?.let { state ->
            when (state) {
                is State.Loading -> {
                    isLoading = true
                }
                is State.Success -> {
                    isLoading = false
                    state.data?.let { qrItem ->
                        activityViewModel.setDetailViewItem(qrItem)
                        // Navigate to detail view
                        navController.navigate(ComposeScreen.DetailView.createRoute(1)) { // FROM_CREATE_CONTEXT
                            popUpTo(ComposeScreen.Create.route)
                        }
                    }
                }
                is State.Error -> {
                    isLoading = false
                    val errorMessage = state.message ?: "Error creating QR code"
                    snackbarHostState.showSnackbar(errorMessage)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_title_email)) },
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
                text = "Create an Email QR Code",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "When scanned, this QR code will open an email client with the information below",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Email Address Field
            OutlinedTextField(
                value = emailAddress,
                onValueChange = { emailAddress = it },
                label = { Text("Email Address *") },
                placeholder = { Text("example@email.com") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled = !isLoading
            )

            // Subject Field
            OutlinedTextField(
                value = emailSubject,
                onValueChange = { emailSubject = it },
                label = { Text("Subject") },
                placeholder = { Text("Email subject") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled = !isLoading
            )

            // Message Field
            OutlinedTextField(
                value = emailText,
                onValueChange = { emailText = it },
                label = { Text("Message") },
                placeholder = { Text("Email message") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                minLines = 5,
                maxLines = 10,
                enabled = !isLoading
            )

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.emailAddress = emailAddress
                        viewModel.emailSubject = emailSubject
                        viewModel.emailText = emailText
                        viewModel.textToQrCodeItem(activityViewModel)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && emailAddress.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text("Create QR Code")
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

// Preview removed - requires ViewModel parameter
