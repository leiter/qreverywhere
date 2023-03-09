package cut.the.crap.qreverywhere.utils.ui

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.annotation.CheckResult
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import cut.the.crap.qreverywhere.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

@ExperimentalCoroutinesApi
@CheckResult
fun EditText.textChanges(): Flow<CharSequence> {
    return callbackFlow {
        val listener = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { trySend(it) }
            }
        }
        addTextChangedListener(listener)
        awaitClose { removeTextChangedListener(listener) }
    }.onStart { emit(text) }
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun ViewBinding.hideKeyboardInput() {
    val inputMethod =
        this.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethod.hideSoftInputFromWindow(this.root.windowToken, 0)
    this.root.clearFocus()
}

fun View.showIme() {
    this.requestFocus()
    val inputMethod = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//    inputMethod.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    inputMethod.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun ImageButton.setDrawable(resId: Int, imageView: ImageButton) {
    val d = ResourcesCompat.getDrawable(resources, resId, imageView.context.theme)
    imageView.setImageDrawable(d)
}

fun View.showSnackBar(config: UiEvent.SnackBar) {
    val s = Snackbar.make(this, config.message, config.duration)
    config.backGroundColor?.let {
        s.view.setBackgroundColor(
            ResourcesCompat.getColor(context.resources, it, null)
        )
    }
    if (config.anchorView != null) {
        s.anchorView = config.anchorView
    }
    if (config.hasAction()) {
        s.setAction(config.actionLabel!!) {
            config.actionBlock!!()
        }
        s.setActionTextColor(ResourcesCompat.getColor(context.resources, R.color.accent, null))
    }
    s.show()
}

sealed class UiEvent {
    data class SnackBar(
        @StringRes val message: Int,
        val duration: Int = Snackbar.LENGTH_LONG,
        val anchorView: View? = null,
        @ColorRes val backGroundColor: Int? = R.color.primary,
        @ColorRes val actionTextColor: Int = R.color.accent,
        @StringRes val actionLabel: Int? = null,
        val actionBlock: (() -> Unit)? = null,
    ) {
        fun hasAction(): Boolean {
            return actionBlock != null && actionLabel != null
        }
    }
}

fun View.registerImeVisibilityListener(
    openAction: () -> Unit,
    closeAction: () -> Unit,
): ViewTreeObserver.OnGlobalLayoutListener {

    var isKeyboardShowing = false

    val globalListener = ViewTreeObserver.OnGlobalLayoutListener {
        val r = Rect()
        getWindowVisibleDisplayFrame(r)
        val screenHeight: Int = rootView.height
        val keypadHeight = screenHeight - r.bottom
        if (keypadHeight > screenHeight * 0.15) {
            if (!isKeyboardShowing) {
                isKeyboardShowing = true
                openAction()
            }
        } else {
            if (isKeyboardShowing) {
                isKeyboardShowing = false
                closeAction()
            }
        }
    }
    viewTreeObserver.addOnGlobalLayoutListener(globalListener)
    return globalListener
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