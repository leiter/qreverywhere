package cut.the.crap.qreverywhere

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.WriterException
import cut.the.crap.qreverywhere.db.QrCodeItem
import cut.the.crap.qreverywhere.repository.QrHistoryRepository
import cut.the.crap.qreverywhere.stuff.Acquire
import cut.the.crap.qreverywhere.stuff.textToImageEncoder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NavigationState {
    object Home : NavigationState()
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val historyRepository: QrHistoryRepository
) : ViewModel(){

    private var _currentScannedQrCodeItem: QrCodeItem = QrCodeItem()

    lateinit var detailviewQrCodeItem: QrCodeItem

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

    @Throws(WriterException::class)
    fun saveQrItemFromFile(textContent: String, resources: Resources, @Acquire.Type type: Int){
        val bitmap = textToImageEncoder(textContent, resources)!!
        val historyItem = QrCodeItem(img = bitmap, textContent = textContent, acquireType = type)
        _currentScannedQrCodeItem = historyItem
        viewModelScope.launch {
            historyRepository.insertQrItem(historyItem)
        }
    }

    fun setDetailViewItem(qrCodeItem: QrCodeItem) {
        detailviewQrCodeItem = qrCodeItem
    }

    fun deleteCurrentDetailView() {
        viewModelScope.launch {
            historyRepository.deleteQrItem(detailviewQrCodeItem)
        }
    }

    fun updateQrItem(qrCodeItem: QrCodeItem) {
        viewModelScope.launch {
            historyRepository.updateQrItem(qrCodeItem)
        }
    }

}