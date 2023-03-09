package cut.the.crap.qreverywhere.utils

import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
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
            clipManager
                ?: (requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).also {
                    if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
                        viewLifecycleOwner.lifecycle.addObserver(this)
                        clipManager = it
                    }
                }

        override fun onDestroy(owner: LifecycleOwner) {
            clipManager = null
        }
    }

fun Fragment.setupMenuItems(
    @MenuRes menuRes: Int,
    idActionMap: Map<Int, () -> Unit>,
    shouldClear: Boolean = false,
    itemsToRemove: List<Int> = emptyList(),
) {
    val menuHost: MenuHost = requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            if (shouldClear) {
                menu.clear()
            } else if (itemsToRemove.isNotEmpty()) {
                itemsToRemove.forEach { menu.removeItem(it) }
            }
            menuInflater.inflate(menuRes, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return idActionMap[menuItem.itemId]?.let { it.invoke(); true } ?: false
        }
    }, viewLifecycleOwner)
}