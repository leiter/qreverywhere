package cut.the.crap.qreverywhere.utils

import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.textfield.TextInputEditText
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun Fragment.setTitle(@StringRes title: Int) {
    (activity as? AppCompatActivity)?.supportActionBar?.setTitle(title)
}
fun Fragment.setTitle(title: String) {
    (activity as? AppCompatActivity)?.supportActionBar?.title = title
}

fun Fragment.setSubTitle(title: String) {
    (activity as? AppCompatActivity)?.supportActionBar?.subtitle = title
}

fun Fragment.focusEditText(editText: EditText) {
    Handler(Looper.getMainLooper()).postDelayed({
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        val manager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }, 50)
}

inline fun <reified V : View> Fragment.activityView(@IdRes viewId: Int): V = requireActivity().findViewById(viewId)

fun Fragment.clipBoard(): ReadOnlyProperty<Fragment, ClipboardManager> =
    object : ReadOnlyProperty<Fragment, ClipboardManager>, DefaultLifecycleObserver {
        private var clipManager: ClipboardManager? = null

        override fun getValue(thisRef: Fragment, property: KProperty<*>): ClipboardManager =
            clipManager ?: (requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).also {
                if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
                    viewLifecycleOwner.lifecycle.addObserver(this)
                    clipManager = it
                }
            }

        override fun onDestroy(owner: LifecycleOwner) {
            clipManager = null
        }
    }

fun TextInputEditText.pasteFromClipBoard(
                                clip: ClipboardManager,
                                onEmptyClipData: (() -> Unit)? = null) {
    val item = clip.primaryClip?.getItemAt(0)?.text
    if (!item.isNullOrBlank()) {
        var currentText = this.text

        if (currentText.isNullOrBlank()) {
            this.append(item)
            this.requestFocus()
        } else {
            val startPos = this.selectionStart
            val endPos = this.selectionEnd
            currentText = currentText.delete(startPos,endPos)
            val start = currentText.substring(0, startPos)
            val end = currentText.substring(startPos)
            val paste = start + item + end
            this.setText("")
            this.append(paste)
            this.requestFocus()
        }
    } else {
        onEmptyClipData?.invoke()
    }
}