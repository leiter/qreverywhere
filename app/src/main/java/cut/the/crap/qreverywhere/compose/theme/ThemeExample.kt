package cut.the.crap.qreverywhere.compose.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Example of how to use QrEveryWhereTheme in your Activity
 *
 * In your Activity's onCreate:
 * ```kotlin
 * class MainActivity : ComponentActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContent {
 *             QrEveryWhereTheme {
 *                 Surface(
 *                     modifier = Modifier.fillMaxSize(),
 *                     color = MaterialTheme.colorScheme.background
 *                 ) {
 *                     MyComposeScreen()
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 */

@Composable
fun ExampleScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "QR EveryWhere",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Compose Theme Example",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Button(onClick = { /* Handle click */ }) {
            Text("Scan QR Code")
        }

        Text(
            text = "This example uses the app's green theme colors",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun ExampleScreenLightPreview() {
    QrEveryWhereTheme(darkTheme = false) {
        Surface {
            ExampleScreen()
        }
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
fun ExampleScreenDarkPreview() {
    QrEveryWhereTheme(darkTheme = true) {
        Surface {
            ExampleScreen()
        }
    }
}
