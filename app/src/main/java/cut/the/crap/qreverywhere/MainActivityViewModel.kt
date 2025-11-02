package cut.the.crap.qreverywhere

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.WriterException
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.utils.data.EncryptedPrefs
import cut.the.crap.qreverywhere.utils.data.textToImageEnc
import cut.the.crap.qrrepository.Acquire
import cut.the.crap.qrrepository.QrHistoryRepository
import cut.the.crap.qrrepository.QrItem
import cut.the.crap.qrrepository.db.QrCodeDbItem
import cut.the.crap.qrrepository.db.toItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

const val IMAGE_DIRECTORY = "QrEveryWhere"

class MainActivityViewModel(
    private val historyRepository: QrHistoryRepository,
    private val encryptedPrefs: EncryptedPrefs,
) : ViewModel() {

    var detailViewQrCodeItem: QrItem = QrCodeDbItem().toItem()
        private set

    // StateFlow for scanned QR code items
    private val _detailViewQrCodeItemState = MutableStateFlow<State<QrItem>?>(null)
    val detailViewQrCodeItemState: StateFlow<State<QrItem>?> = _detailViewQrCodeItemState.asStateFlow()

    // SharedFlow for one-time save events (like SingleLiveDataEvent)
    private val _saveQrImageEvent = MutableSharedFlow<State<String?>>()
    val saveQrImageEvent: SharedFlow<State<String?>> = _saveQrImageEvent.asSharedFlow()

    // StateFlow for history data
    private val _historyData = MutableStateFlow<List<QrItem>>(emptyList())
    val historyData: StateFlow<List<QrItem>> = _historyData.asStateFlow()

    init {
        // Collect repository flow and update StateFlow
        viewModelScope.launch {
            historyRepository.getCompleteQrCodeHistory().collect { items ->
                _historyData.value = items
            }
        }
    }

    fun saveQrItem(qrCodeItem: QrItem) {
        viewModelScope.launch {
            historyRepository.insertQrItem(qrCodeItem)
        }
    }

    @Throws(WriterException::class)
    fun saveQrItemFromFile(textContent: String, @Acquire.Type type: Int) {
        if (Acquire.FROM_FILE == type) _detailViewQrCodeItemState.value = State.loading()
        viewModelScope.launch {
            val bitmap = textToImageEnc(textContent, encryptedPrefs.foregroundColor, encryptedPrefs.backgroundColor)
            val historyItem = QrCodeDbItem(
                img = bitmap,
                textContent = textContent,
                acquireType = type)
                .toItem()
            detailViewQrCodeItem = historyItem
            _detailViewQrCodeItemState.value = State.success(historyItem)
            historyRepository.insertQrItem(historyItem)
        }
    }

    fun setDetailViewItem(qrCodeItem: QrItem) {
        detailViewQrCodeItem = qrCodeItem
    }

    fun deleteCurrentDetailView() {
        val pos = _historyData.value.indexOf(detailViewQrCodeItem)
        if (pos >= 0) {
            removeHistoryItem(pos)
        } else {
            Timber.w("Could not find item to delete")
        }
    }

    fun saveQrImageOfDetailView(context: Context) {
        viewModelScope.launch {
            _saveQrImageEvent.emit(State.loading())
            val imageUri = saveImageToFile(detailViewQrCodeItem, context)
            _saveQrImageEvent.emit(State.success(data = imageUri))
            val updateItem = detailViewQrCodeItem.copy(fileUriString = imageUri)
            historyRepository.updateQrItem(updateItem)
            detailViewQrCodeItem = updateItem
        }
    }

    fun removeHistoryItem(pos: Int) {
        if (pos > -1) {
            viewModelScope.launch {
                val items = _historyData.value
                if (pos < items.size) {
                    historyRepository.deleteQrItem(items[pos])
                    Timber.d("Deleted QR item at position $pos")
                }
            }
        }
    }

    private suspend fun saveImageToFile(qrCodeItem: QrItem, context: Context): String {
        return withContext(Dispatchers.IO) {

            val bytes = ByteArrayOutputStream()
            qrCodeItem.img.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                IMAGE_DIRECTORY
            )

            if (!directory.exists()) {
                directory.mkdirs()
            }
            try {
                val file = File(
                    directory, Calendar.getInstance().timeInMillis.toString() + ".jpg"
                )
                file.createNewFile()
                FileOutputStream(file).use { it.write(bytes.toByteArray()) }
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.path),
                    arrayOf("image/jpeg")
                ) { _, uri -> Timber.d("MediaScanner::--->$uri") }
//                Timber.d("File Saved::--->" + file.absolutePath)
                file.absolutePath
            } catch (e1: IOException) {
                e1.printStackTrace()
                ""
            }
        }
    }
}

class CouldNotDeleteQrItem : Exception()