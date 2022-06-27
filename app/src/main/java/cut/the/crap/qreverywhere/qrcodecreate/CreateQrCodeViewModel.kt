package cut.the.crap.qreverywhere.qrcodecreate

import android.content.res.Resources
import android.graphics.Bitmap
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


@HiltViewModel
class CreateQrCodeViewModel @Inject constructor(
    private val qrHistoryRepository: QrHistoryRepository
) : ViewModel() {

    @Throws(WriterException::class)
    fun textToImageEncode(textContent: String, resources: Resources): Bitmap? {
        val bitmap = textToImageEncoder(textContent, resources)!!
        val historyItem = QrCodeItem(img = bitmap, textContent = textContent, acquireType = Acquire.CREATED)
        viewModelScope.launch {
            qrHistoryRepository.insertQrItem(historyItem)
        }
        return bitmap
    }

    companion object {

    }


}