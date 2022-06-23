package cut.the.crap.qreverywhere

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.db.QrCodeItem
import cut.the.crap.qreverywhere.repository.QrHistoryRepository
import cut.the.crap.qreverywhere.repository.QrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class NavigationState {
    object Home : NavigationState()
}


@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val qrRepository: QrRepository,
    private val historyRepository: QrHistoryRepository
) : ViewModel(){

    private val _loadingState : MutableStateFlow<State<Unit>> = MutableStateFlow(State.loading())
    private val loadingState : StateFlow<*>
    get() = _loadingState

    var showCamera = true

    fun shouldStartCamera() : Boolean {

//        prefs and permission
        return showCamera
    }


    val historyAdapterData = historyRepository.getCompleteQrCodeHistory()

    fun saveQrItem(qrCodeItem: QrCodeItem) {
        viewModelScope.launch {
            historyRepository.insertQrItem(qrCodeItem)
        }
    }

    fun deleteQrItem(qrCodeItem: QrCodeItem) {
        viewModelScope.launch {
            historyRepository.deleteQrItem(qrCodeItem)
        }
    }


}