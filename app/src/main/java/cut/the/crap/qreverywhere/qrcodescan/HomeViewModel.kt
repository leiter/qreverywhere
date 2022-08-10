package cut.the.crap.qreverywhere.qrcodescan

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.WriterException
import cut.the.crap.qreverywhere.db.QrCodeItem
import cut.the.crap.qreverywhere.repository.QrHistoryRepository
import cut.the.crap.qreverywhere.utils.Acquire
import cut.the.crap.qreverywhere.utils.textToImageEnc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val qrHistoryRepository: QrHistoryRepository
) : ViewModel() {

    lateinit var currentQrCodeItem: QrCodeItem

    @Throws(WriterException::class)
    fun saveQrItemFromFile(textContent: String, resources: Resources){

        viewModelScope.launch {
            val bitmap = textToImageEnc(textContent, resources)!!
            val historyItem = QrCodeItem(img = bitmap, textContent = textContent, acquireType = Acquire.CREATED)
            currentQrCodeItem = historyItem
            qrHistoryRepository.insertQrItem(historyItem)
        }
    }

}