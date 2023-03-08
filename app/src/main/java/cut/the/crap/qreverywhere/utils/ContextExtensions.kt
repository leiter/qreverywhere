package cut.the.crap.qreverywhere.utils

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

fun Context.showShortToast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showLongToast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.hasPermission(permissionString: String) =
    ContextCompat.checkSelfPermission(
        this, permissionString
    ) == PackageManager.PERMISSION_GRANTED
