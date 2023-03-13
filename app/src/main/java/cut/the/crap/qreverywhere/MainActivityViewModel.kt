package cut.the.crap.qreverywhere

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.zxing.WriterException
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.utils.data.EncryptedPrefs
import cut.the.crap.qreverywhere.utils.data.SingleLiveDataEvent
import cut.the.crap.qreverywhere.utils.textToImageEnc
import cut.the.crap.qrrepository.Acquire
import cut.the.crap.qrrepository.QrHistoryRepository
import cut.the.crap.qrrepository.QrItem
import cut.the.crap.qrrepository.db.QrCodeDbItem
import cut.the.crap.qrrepository.db.toItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import javax.inject.Inject

const val IMAGE_DIRECTORY = "QrEveryWhere"

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val historyRepository: QrHistoryRepository,
    private val encryptedPrefs: EncryptedPrefs
) : ViewModel() {

    var detailViewQrCodeItem: QrItem = QrCodeDbItem().toItem()
        private set

    val detailViewLiveQrCodeItem = MutableLiveData<State<QrItem>>()

    val saveDetailViewQrCodeImage = SingleLiveDataEvent<State<String?>>(null)

    private val startDetailViewQrCodeItem = SingleLiveDataEvent<QrItem?>(null)

    val historyAdapterData = historyRepository.getCompleteQrCodeHistory().asLiveData(Dispatchers.Main)

    val removeItemSingleLiveDataEvent = SingleLiveDataEvent<State<QrItem>>(null)

    fun saveQrItem(qrCodeItem: QrItem) {
        viewModelScope.launch {
            historyRepository.insertQrItem(qrCodeItem)
        }
    }

    @Throws(WriterException::class)
    fun saveQrItemFromFile(textContent: String, @Acquire.Type type: Int) {
        if (Acquire.FROM_FILE == type) detailViewLiveQrCodeItem.value = State.loading()
        viewModelScope.launch {
            val bitmap = textToImageEnc(textContent, encryptedPrefs.foregroundColor, encryptedPrefs.backgroundColor)
            val historyItem = QrCodeDbItem(
                img = bitmap,
                textContent = textContent,
                acquireType = type)
                .toItem()
            detailViewQrCodeItem = historyItem
            startDetailViewQrCodeItem.value = historyItem
            detailViewLiveQrCodeItem.value = State.success(historyItem)
            historyRepository.insertQrItem(historyItem)
        }
    }

    fun setDetailViewItem(qrCodeItem: QrItem) {
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
                val f = File(
                    directory, Calendar.getInstance().timeInMillis.toString() + ".jpg"
                )
                f.createNewFile()
                val fo = FileOutputStream(f)
                fo.write(bytes.toByteArray())
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(f.path),
                    arrayOf("image/jpeg")
                ) { path, uri ->
                    val updateQritem = qrCodeItem.copy(fileUriString = uri.toString())
                    Timber.e("MediaScanner::--->$uri")
                }
                fo.close()
                Timber.d("File Saved::--->" + f.absolutePath)
                f.absolutePath
            } catch (e1: IOException) {
                e1.printStackTrace()
                ""
            }
        }
    }
}

class CouldNotDeleteQrItem : Exception()