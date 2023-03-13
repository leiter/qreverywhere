package cut.the.crap.qreverywhere.utils.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import cut.the.crap.qreverywhere.BuildConfig

fun Context.showShortToast(@StringRes message: Int, throwable: Throwable? = null) {
    val msg = if(BuildConfig.DEBUG && throwable != null) getString(message) else getString(message)
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.showLongToast(@StringRes message: Int, throwable: Throwable? = null) {
    val msg = if(BuildConfig.DEBUG && throwable != null) getString(message) else getString(message)
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun Context.hasPermission(permissionString: String) =
    ContextCompat.checkSelfPermission(
        this, permissionString
    ) == PackageManager.PERMISSION_GRANTED

fun Context.isLandscape() : Boolean {
    return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun Context.startIntentGracefully(intent: Intent, notExecutable: (() -> Unit)? = null) {
    val list = this.packageManager.queryIntentActivities(
        intent,
        PackageManager.MATCH_DEFAULT_ONLY
    )
    if (list.size > 0) {
        startActivity(intent)
    } else {
        notExecutable?.invoke()
    }
}