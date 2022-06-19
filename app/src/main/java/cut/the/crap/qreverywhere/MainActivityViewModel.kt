package cut.the.crap.qreverywhere

import androidx.lifecycle.ViewModel
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.repository.QrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


sealed class NavigationState {
    object Home : NavigationState()
}


@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val qrRepository: QrRepository,
) : ViewModel(){

    private val _loadingState : MutableStateFlow<State<Unit>> = MutableStateFlow(State.loading())
    private val loadingState : StateFlow<*>
    get() = _loadingState



    fun shouldStartCamera() : Boolean {

//        prefs and permission
        return true
    }



}