package cut.the.crap.qreverywhere.utils.data

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import cut.the.crap.qreverywhere.BuildConfig

sealed class IntentGenerator {

    abstract fun getIntent() : Intent

    object OpenAppSettings : IntentGenerator() {
        override fun getIntent(): Intent {
            return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_NO_HISTORY
                    or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                )
            }
        }
    }
    data class QrStartIntent(val encodedString: String) : IntentGenerator() {
        private val decoded = Uri.decode(encodedString)
        override fun getIntent(): Intent {
            return when {
                decoded.startsWith("tel:") -> Intent(Intent.ACTION_DIAL, Uri.parse(encodedString))
                decoded.startsWith("mailto:") -> Intent(Intent.ACTION_SENDTO, Uri.parse(encodedString))
                decoded.startsWith("http:") -> Intent(Intent.ACTION_VIEW, Uri.parse(decoded))
                decoded.startsWith("https:") -> Intent(Intent.ACTION_VIEW, Uri.parse(decoded))
                decoded.startsWith("sms:") -> Intent(Intent.ACTION_VIEW, Uri.parse(encodedString))
                decoded.startsWith("smsto:") -> Intent(Intent.ACTION_SENDTO, Uri.parse(encodedString))
                else -> Intent(Intent.ACTION_VIEW, Uri.parse(encodedString))
            }
        }
    }

    object PickImageIntent : IntentGenerator() {
        override fun getIntent(): Intent {
            return Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
        }

    }
}