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
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun Fragment.setTitle(@StringRes title: Int) {
    (activity as? AppCompatActivity)?.supportActionBar?.setTitle(title)
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
        private var binding: ClipboardManager? = null

        override fun getValue(thisRef: Fragment, property: KProperty<*>): ClipboardManager =
            binding ?: (requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).also {
                if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
                    viewLifecycleOwner.lifecycle.addObserver(this)
                    binding = it
                }
            }

        override fun onDestroy(owner: LifecycleOwner) {
            binding = null
        }
    }