package cut.the.crap.qreverywhere

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.WriterException
import cut.the.crap.qreverywhere.db.QrCodeItem
import cut.the.crap.qreverywhere.repository.QrHistoryRepository
import cut.the.crap.qreverywhere.stuff.Acquire
import cut.the.crap.qreverywhere.stuff.SingleLiveDataEvent
import cut.the.crap.qreverywhere.stuff.saveImageToFile
import cut.the.crap.qreverywhere.stuff.textToImageEnc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NavigationState {
    object Home : NavigationState()
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val historyRepository: QrHistoryRepository
) : ViewModel() {

    private var _currentScannedQrCodeItem: QrCodeItem = QrCodeItem()

    lateinit var detailViewQrCodeItem: QrCodeItem

    val  startDetailViewQrCodeItem = SingleLiveDataEvent<QrCodeItem?>(null)

    val historyAdapterData = historyRepository.getCompleteQrCodeHistory()

    val progressIndication = SingleLiveDataEvent<Boolean?>(null)

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

    fun updateQrItem(qrCodeItem: QrCodeItem) {
        viewModelScope.launch {
            historyRepository.updateQrItem(qrCodeItem)
        }
    }

    @Throws(WriterException::class)
    fun saveQrItemFromFile(textContent: String, resources: Resources, @Acquire.Type type: Int) {
        viewModelScope.launch {
            val bitmap = textToImageEnc(textContent, resources)
            val historyItem = QrCodeItem(img = bitmap, textContent = textContent, acquireType = type)
            detailViewQrCodeItem = historyItem
            startDetailViewQrCodeItem.value = historyItem
            historyRepository.insertQrItem(historyItem)
        }
    }

    fun setDetailViewItem(qrCodeItem: QrCodeItem) {
        detailViewQrCodeItem = qrCodeItem
    }

    fun deleteCurrentDetailView() {
        progressIndication.value = true
        viewModelScope.launch {
            historyRepository.deleteQrItem(detailViewQrCodeItem)
            progressIndication.value = false
        }
    }

    fun saveQrImageOfDetailView(context: Context) {
        saveImageToFile(detailViewQrCodeItem, context)
    }

    fun removeHistoryItem(pos: Int) : QrCodeItem? {
        var result: QrCodeItem? = null
        progressIndication.value = true
        viewModelScope.launch {
            historyAdapterData.value?.let {
                result = it[pos]
                historyRepository.deleteQrItem(it[pos])
                progressIndication.value = false
            }

        }
        return result
    }

    fun provideListItem(pos: Int): QrCodeItem? {
        historyAdapterData.value?.let {
            return it[pos]
        } ?: return null
    }


}