package cut.the.crap.qreverywhere.utils

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