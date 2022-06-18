package cut.the.crap.qreverywhere

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
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

    private val _navigationState = MutableLiveData<NavigationState>().apply {
        value = NavigationState.Home
    }


    private val _loadingState : MutableStateFlow<State<Unit>> = MutableStateFlow(State.loading())
    private val loadingState : StateFlow<*>
    get() = _loadingState


    fun scanQrImage(uri: Uri) {

        qrRepository.scanQrImage(uri)
    }


    fun scanQrImage(uri: Uri, context: Context): String? {
        var contents: String? = null

        val inputStream = context.contentResolver.openInputStream(uri)
        val sourceBitmap: Bitmap = BitmapFactory.decodeStream(inputStream)

        val intArray = IntArray(sourceBitmap.width * sourceBitmap.height)
        //copy pixel data from the Bitmap into the 'intArray' array
        //copy pixel data from the Bitmap into the 'intArray' array
        sourceBitmap.getPixels(intArray, 0, sourceBitmap.width, 0, 0, sourceBitmap.width, sourceBitmap.height)

        val source: LuminanceSource =
            RGBLuminanceSource(sourceBitmap.width, sourceBitmap.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        val reader: Reader = MultiFormatReader()
        try {
            val result: Result = reader.decode(bitmap)
            contents = result.text
        } catch (e: Exception) {
            Log.e("QrTest", "Error decoding barcode", e)
        }
        return contents
    }



}