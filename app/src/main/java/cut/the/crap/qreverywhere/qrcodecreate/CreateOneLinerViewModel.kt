package cut.the.crap.qreverywhere.qrcodecreate

import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.db.QrCodeItem
import cut.the.crap.qreverywhere.repository.QrHistoryRepository
import cut.the.crap.qreverywhere.stuff.Acquire
import cut.the.crap.qreverywhere.stuff.textToImageEnc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateOneLinerViewModel @Inject constructor(
    private val historyRepository: QrHistoryRepository
) : ViewModel() {

    val qrCodeItemState = MutableLiveData<State<QrCodeItem>>()
    var currentInputText = ""
    var currentInputNumber = ""

    private fun createWebQrcode() {

    }

    private fun createSMSQrcode() {

    }

    private fun createCallQrcode() {

    }

    private fun testWebQrcode(resources: Resources) {
        startLoading()
    }

    private fun testSMSQrcode(resources: Resources) {
        startLoading()
    }

    private fun testCallQrcode(resources: Resources) {
        val isValid = android.util.Patterns.PHONE.matcher(currentInputNumber).matches()
        if(isValid){
            viewModelScope.launch {
                val uriString = TEL_PREFIX + currentInputNumber
                val bitmap = textToImageEnc(uriString, resources)
                val qrCodeItem = QrCodeItem(img = bitmap, textContent = uriString, acquireType = Acquire.CREATED)
                qrCodeItemState.value = State.success(qrCodeItem)
            }
        } else {
            qrCodeItemState.value = State.error()
        }
    }

    private fun startLoading(){
        qrCodeItemState.value = State.loading()
    }

    fun createClicked(mode: Int){
        when(mode){
            CreateOneLinerFragment.CREATE_PHONE -> createCallQrcode()
            CreateOneLinerFragment.CREATE_SMS -> createSMSQrcode()
            CreateOneLinerFragment.CREATE_WEB -> createWebQrcode()

            else -> throw IllegalArgumentException("No function associated with mode $mode")
        }
    }

    fun testClicked(mode: Int, resources: Resources) {
        when(mode){
            CreateOneLinerFragment.CREATE_PHONE -> testCallQrcode(resources)
            CreateOneLinerFragment.CREATE_SMS -> testSMSQrcode(resources)
            CreateOneLinerFragment.CREATE_WEB -> testWebQrcode(resources)

            else -> throw IllegalArgumentException("No function associated with mode $mode")
        }
    }

    private companion object {
        const val SMS_PREFIX = "sms:"
        const val TEL_PREFIX = "tel:"
    }

}

