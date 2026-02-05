@file:OptIn(ExperimentalMaterial3Api::class)

package cut.the.crap.qreverywhere.web

import androidx.compose.foundation.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.CanvasBasedWindow
import kotlinx.coroutines.launch
import org.jetbrains.skia.Image

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget", title = "QR Everywhere") {
        MaterialTheme {
            QrGeneratorApp()
        }
    }
}

@Composable
fun QrGeneratorApp() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("QR Everywhere") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Text/URL", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("WiFi", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Contact", modifier = Modifier.padding(16.dp))
                }
            }

            // Content
            when (selectedTab) {
                0 -> TextQrScreen()
                1 -> WiFiQrScreen()
                2 -> ContactQrScreen()
            }
        }
    }
}

@Composable
fun TextQrScreen() {
    var text by remember { mutableStateOf("") }
    var qrImageData by remember { mutableStateOf<ByteArray?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Enter text or URL to generate QR code",
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Text or URL") },
            placeholder = { Text("https://example.com") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5
        )

        Button(
            onClick = {
                if (text.isNotBlank()) {
                    isGenerating = true
                    scope.launch {
                        qrImageData = generateQrCode(text)
                        isGenerating = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = text.isNotBlank() && !isGenerating
        ) {
            Text(if (isGenerating) "Generating..." else "Generate QR Code")
        }

        QrCodeDisplay(qrImageData)
    }
}

@Composable
fun WiFiQrScreen() {
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var securityType by remember { mutableStateOf("WPA") }
    var isHidden by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var qrImageData by remember { mutableStateOf<ByteArray?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Enter WiFi network details",
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
            value = ssid,
            onValueChange = { ssid = it },
            label = { Text("Network Name (SSID)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Security Type
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = securityType == "WPA",
                onClick = { securityType = "WPA" },
                label = { Text("WPA/WPA2") }
            )
            FilterChip(
                selected = securityType == "WEP",
                onClick = { securityType = "WEP" },
                label = { Text("WEP") }
            )
            FilterChip(
                selected = securityType == "nopass",
                onClick = { securityType = "nopass" },
                label = { Text("Open") }
            )
        }

        if (securityType != "nopass") {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showPassword = !showPassword }) {
                        Text(if (showPassword) "Hide" else "Show")
                    }
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isHidden, onCheckedChange = { isHidden = it })
            Text("Hidden Network")
        }

        Button(
            onClick = {
                if (ssid.isNotBlank()) {
                    isGenerating = true
                    scope.launch {
                        val wifiString = buildWiFiString(ssid, password, securityType, isHidden)
                        qrImageData = generateQrCode(wifiString)
                        isGenerating = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = ssid.isNotBlank() && !isGenerating
        ) {
            Text(if (isGenerating) "Generating..." else "Generate QR Code")
        }

        QrCodeDisplay(qrImageData)
    }
}

@Composable
fun ContactQrScreen() {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var qrImageData by remember { mutableStateOf<ByteArray?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Enter contact information",
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = organization,
            onValueChange = { organization = it },
            label = { Text("Organization (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                if (firstName.isNotBlank() || lastName.isNotBlank()) {
                    isGenerating = true
                    scope.launch {
                        val vcard = buildVCard(firstName, lastName, phone, email, organization)
                        qrImageData = generateQrCode(vcard)
                        isGenerating = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = (firstName.isNotBlank() || lastName.isNotBlank()) && !isGenerating
        ) {
            Text(if (isGenerating) "Generating..." else "Generate QR Code")
        }

        QrCodeDisplay(qrImageData)
    }
}

@Composable
fun QrCodeDisplay(imageData: ByteArray?) {
    if (imageData != null && imageData.isNotEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val imageBitmap = remember(imageData) {
                try {
                    Image.makeFromEncoded(imageData).toComposeImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }

            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "QR Code",
                    modifier = Modifier.size(256.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { downloadImage(imageData, "qrcode.png") }) {
                        Text("Download")
                    }
                    OutlinedButton(onClick = { copyImageToClipboard(imageData) }) {
                        Text("Copy")
                    }
                }
            }
        }
    }
}

// Helper functions
private fun buildWiFiString(ssid: String, password: String, security: String, hidden: Boolean): String {
    return buildString {
        append("WIFI:")
        append("T:$security;")
        append("S:${escapeWiFi(ssid)};")
        if (security != "nopass" && password.isNotEmpty()) {
            append("P:${escapeWiFi(password)};")
        }
        if (hidden) {
            append("H:true;")
        }
        append(";")
    }
}

private fun escapeWiFi(text: String): String {
    return text
        .replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(":", "\\:")
        .replace(",", "\\,")
        .replace("\"", "\\\"")
}

private fun buildVCard(firstName: String, lastName: String, phone: String, email: String, org: String): String {
    return buildString {
        appendLine("BEGIN:VCARD")
        appendLine("VERSION:3.0")
        appendLine("N:$lastName;$firstName;;;")
        val fn = listOfNotNull(
            firstName.takeIf { it.isNotEmpty() },
            lastName.takeIf { it.isNotEmpty() }
        ).joinToString(" ")
        appendLine("FN:$fn")
        if (phone.isNotEmpty()) appendLine("TEL:$phone")
        if (email.isNotEmpty()) appendLine("EMAIL:$email")
        if (org.isNotEmpty()) appendLine("ORG:$org")
        append("END:VCARD")
    }
}

// JS interop for QR code generation
private suspend fun generateQrCode(text: String): ByteArray {
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        generateQrCodeJs(
            text = text,
            size = 512,
            onSuccess = { dataUrl ->
                val base64 = dataUrl.substringAfter("base64,")
                val bytes = decodeBase64(base64)
                continuation.resume(bytes, null)
            },
            onError = {
                continuation.resume(ByteArray(0), null)
            }
        )
    }
}

@JsFun("""
(text, size, onSuccess, onError) => {
    if (typeof QRCode !== 'undefined') {
        QRCode.toDataURL(text, { width: size, height: size, margin: 1 })
            .then(onSuccess)
            .catch(err => onError(err.message));
    } else {
        onError('QRCode library not loaded');
    }
}
""")
private external fun generateQrCodeJs(text: String, size: Int, onSuccess: (String) -> Unit, onError: (String) -> Unit)

private fun decodeBase64(base64: String): ByteArray {
    val jsArray = decodeBase64Js(base64)
    val length = jsArrayLength(jsArray)
    return ByteArray(length) { jsArrayGet(jsArray, it).toByte() }
}

@JsFun("(base64) => { const b = atob(base64); const arr = new Uint8Array(b.length); for (let i = 0; i < b.length; i++) arr[i] = b.charCodeAt(i); return arr; }")
private external fun decodeBase64Js(base64: String): JsAny

@JsFun("(arr) => arr.length")
private external fun jsArrayLength(arr: JsAny): Int

@JsFun("(arr, i) => arr[i]")
private external fun jsArrayGet(arr: JsAny, index: Int): Int

// Download and clipboard functions
private fun downloadImage(data: ByteArray, fileName: String) {
    val jsArray = byteArrayToJsArray(data)
    downloadImageJs(jsArray, fileName)
}

@JsFun("""
(data, fileName) => {
    const blob = new Blob([data], { type: 'image/png' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}
""")
private external fun downloadImageJs(data: JsAny, fileName: String)

private fun copyImageToClipboard(data: ByteArray) {
    val jsArray = byteArrayToJsArray(data)
    copyImageToClipboardJs(jsArray)
}

@JsFun("""
(data) => {
    const blob = new Blob([data], { type: 'image/png' });
    navigator.clipboard.write([new ClipboardItem({ 'image/png': blob })]).catch(console.error);
}
""")
private external fun copyImageToClipboardJs(data: JsAny)

private fun byteArrayToJsArray(data: ByteArray): JsAny {
    val jsArray = createUint8Array(data.size)
    for (i in data.indices) {
        setUint8ArrayValue(jsArray, i, data[i].toInt() and 0xFF)
    }
    return jsArray
}

@JsFun("(size) => new Uint8Array(size)")
private external fun createUint8Array(size: Int): JsAny

@JsFun("(arr, i, v) => { arr[i] = v; }")
private external fun setUint8ArrayValue(arr: JsAny, index: Int, value: Int)
