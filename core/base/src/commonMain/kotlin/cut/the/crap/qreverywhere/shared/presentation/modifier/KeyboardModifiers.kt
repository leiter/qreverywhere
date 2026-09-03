package cut.the.crap.qreverywhere.shared.presentation.modifier

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/**
 * Clears text-field focus and hides the soft keyboard when the user taps a
 * non-interactive area of the composable this is applied to.
 *
 * Interactive descendants (text fields, buttons, clickables) consume tap events
 * before they reach this handler, so only taps on "empty" space trigger the
 * dismissal. Apply this once around the navigation host so every screen inherits
 * the behaviour.
 *
 * `focusManager.clearFocus()` covers Android/Desktop; the explicit
 * `keyboardController.hide()` is needed on iOS, where clearing focus alone does
 * not always retract the keyboard.
 */
fun Modifier.dismissKeyboardOnTap(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    pointerInput(Unit) {
        detectTapGestures(
            onTap = {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        )
    }
}
