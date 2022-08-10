package cut.the.crap.qreverywhere.qrdelegates

import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import cut.the.crap.qreverywhere.utils.registerImeVisibilityListener

class ImeActionDelegateImpl : ImeActionDelegate {

    private lateinit var fragment: Fragment

    private lateinit var viewTreeObserverListener: ViewTreeObserver.OnGlobalLayoutListener

    private lateinit var openImeAction: () -> Unit

    private lateinit var closeImeAction: () -> Unit

    private val lifecycleEventObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            viewTreeObserverListener = fragment.requireView().registerImeVisibilityListener(openImeAction, closeImeAction)
        }
        else if (event == Lifecycle.Event.ON_PAUSE) {
            fragment.requireView().viewTreeObserver.removeOnGlobalLayoutListener(viewTreeObserverListener)
        }
    }

    override fun attachImeActionDelegate(fragment: Fragment, openImeAction: () -> Unit, closeImeAction: () -> Unit ){
        this.fragment = fragment
        this.openImeAction = openImeAction
        this.closeImeAction = closeImeAction
        fragment.lifecycle.addObserver(lifecycleEventObserver)
    }

}