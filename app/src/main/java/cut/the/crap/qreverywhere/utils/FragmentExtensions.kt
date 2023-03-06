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

fun pasteFromClipBoard(textInput: TextInputEditText,
                                clip: ClipboardManager,
                                onEmptyClipData: (() -> Unit)? = null) {
    val item = clip.primaryClip?.getItemAt(0)?.text
    if (!item.isNullOrBlank()) {
        var currentText = textInput.text

        if (currentText.isNullOrBlank()) {
            textInput.setText(item)
        } else {
            val startPos = textInput.selectionStart
            val endPos = textInput.selectionEnd
            currentText = currentText.delete(startPos,endPos)
            val start = currentText.substring(0, startPos)
            val end = currentText.substring(startPos)
            val paste = start + item + end
            textInput.setText("")
            textInput.append(paste)
            textInput.requestFocus()
        }
    } else {
        onEmptyClipData?.invoke()
    }
}