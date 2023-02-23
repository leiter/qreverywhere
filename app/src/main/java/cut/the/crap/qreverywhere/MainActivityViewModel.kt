package cut.the.crap.qreverywhere

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.WriterException
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qrrepository.Acquire
import cut.the.crap.qreverywhere.utils.SingleLiveDataEvent
import cut.the.crap.qreverywhere.utils.saveImageToFile
import cut.the.crap.qreverywhere.utils.textToImageEnc
import cut.the.crap.qrrepository.QrHistoryRepository
import cut.the.crap.qrrepository.QrItem
import cut.the.crap.qrrepository.db.QrCodeDbItem
import cut.the.crap.qrrepository.db.toItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val historyRepository: QrHistoryRepository,
) : ViewModel() {

    var focusedItemIndex: Int = 0

    var detailViewQrCodeItem: QrItem = QrCodeDbItem().toItem()

    val detailViewLiveQrCodeItem = MutableLiveData<State<QrItem>>()

    val saveDetailViewQrCodeImage = SingleLiveDataEvent<State<String?>>(null)

    val startDetailViewQrCodeItem = SingleLiveDataEvent<QrItem?>(null)

    val historyAdapterData = historyRepository.getCompleteQrCodeHistory()

    val removeItemSingleLiveDataEvent = SingleLiveDataEvent<State<QrItem>>(null)

    fun saveQrItem(qrCodeItem: QrItem) {
        viewModelScope.launch {
            historyRepository.insertQrItem(qrCodeItem)
        }
    }

    @Throws(WriterException::class)
    fun saveQrItemFromFile(textContent: String, resources: Resources, @Acquire.Type type: Int) {
        if (Acquire.FROM_FILE == type) detailViewLiveQrCodeItem.value = State.loading()
        viewModelScope.launch {
            val bitmap = textToImageEnc(textContent, resources)
            val historyItem = QrCodeDbItem(img = bitmap, textContent = textContent, acquireType = type).toItem()
            detailViewQrCodeItem = historyItem
            startDetailViewQrCodeItem.value = historyItem
            detailViewLiveQrCodeItem.value = State.success(historyItem)
            historyRepository.insertQrItem(historyItem)
        }
    }

    fun setDetailViewItem(qrCodeItem: QrItem) {
        focusedItemIndex = historyAdapterData.value?.indexOf(qrCodeItem) ?: 0
        detailViewQrCodeItem = qrCodeItem
    }

    fun deleteCurrentDetailView() {
        val pos = historyAdapterData.value?.indexOf(detailViewQrCodeItem)
        pos?.let {
            removeHistoryItem(it)
        } ?: kotlin.run {
            removeItemSingleLiveDataEvent.value = State.error(error = CouldNotDeleteQrItem())
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
        if (pos > -1) {
            var result: QrItem?
            removeItemSingleLiveDataEvent.value = State.loading()
            viewModelScope.launch {
                historyAdapterData.value?.let {
                    result = it[pos]
                    historyRepository.deleteQrItem(it[pos])
                    removeItemSingleLiveDataEvent.value = State.success(result)
                }
            }
        }

    }

}

class CouldNotDeleteQrItem : Exception()