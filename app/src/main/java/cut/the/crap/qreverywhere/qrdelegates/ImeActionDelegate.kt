package cut.the.crap.qreverywhere.qrdelegates

import androidx.fragment.app.Fragment

interface ImeActionDelegate {
    fun attachImeActionDelegate(fragment: Fragment, openImeAction: () -> Unit, closeImeAction: () -> Unit )
}

