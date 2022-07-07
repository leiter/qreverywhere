package cut.the.crap.qreverywhere

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.WriterException
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.db.QrCodeItem
import cut.the.crap.qreverywhere.repository.QrHistoryRepository
import cut.the.crap.qreverywhere.stuff.Acquire
import cut.the.crap.qreverywhere.stuff.SingleLiveDataEvent
import cut.the.crap.qreverywhere.stuff.saveImageToFile
import cut.the.crap.qreverywhere.stuff.textToImageEnc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val historyRepository: QrHistoryRepository
) : ViewModel() {

    var focusedItemIndex: Int = 0

    var detailViewQrCodeItem: QrCodeItem = QrCodeItem()

    val detailViewLiveQrCodeItem = MutableLiveData<State<QrCodeItem>>()

    val saveDetailViewQrCodeImage = SingleLiveDataEvent<State<String?>>(null)

    val startDetailViewQrCodeItem = SingleLiveDataEvent<QrCodeItem?>(null)

    val historyAdapterData = historyRepository.getCompleteQrCodeHistory()

    val removeItemSingleLiveDataEvent = SingleLiveDataEvent<State<QrCodeItem>>(null)

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
        if (Acquire.FROM_FILE == type) detailViewLiveQrCodeItem.value = State.loading()
        viewModelScope.launch {
            val bitmap = textToImageEnc(textContent, resources)
            val historyItem =
                QrCodeItem(img = bitmap, textContent = textContent, acquireType = type)
            detailViewQrCodeItem = historyItem
            startDetailViewQrCodeItem.value = historyItem
            detailViewLiveQrCodeItem.value = State.success(historyItem)
            historyRepository.insertQrItem(historyItem)
        }
    }

    fun setDetailViewItem(qrCodeItem: QrCodeItem) {
        focusedItemIndex = historyAdapterData.value?.indexOf(qrCodeItem) ?: 0
        detailViewQrCodeItem = qrCodeItem
    }

    fun deleteCurrentDetailView() {
        val pos = historyAdapterData.value?.indexOf(detailViewQrCodeItem)
        viewModelScope.launch {
            pos?.let {
                removeHistoryItem(it)
            } ?: kotlin.run {
                removeItemSingleLiveDataEvent.value = State.error(error = CouldNotDeleteQrItem())
            }
        }
    }

    fun saveQrImageOfDetailView(context: Context) {
        saveDetailViewQrCodeImage.value = State.loading()
        viewModelScope.launch {
            val imageUri = saveImageToFile(detailViewQrCodeItem, context)
            saveDetailViewQrCodeImage.value = State.success(data = imageUri)
            val updateItem = detailViewQrCodeItem.copy(fileUriString = imageUri)
            historyRepository.updateQrItem(updateItem)
            detailViewQrCodeItem = updateItem
        }
    }

    fun removeHistoryItem(pos: Int) {
        var result: QrCodeItem? = null
        removeItemSingleLiveDataEvent.value = State.loading()
        viewModelScope.launch {
            historyAdapterData.value?.let {
                result = it[pos]
                historyRepository.deleteQrItem(it[pos])
                removeItemSingleLiveDataEvent.value = State.success(result)
            }
        }
    }

    fun setLatestItemAsDetail() {

            historyAdapterData.value?.let {
                if (it.isNotEmpty())
                    detailViewQrCodeItem = it[0]
            }

    }

}

class CouldNotDeleteQrItem : Exception()