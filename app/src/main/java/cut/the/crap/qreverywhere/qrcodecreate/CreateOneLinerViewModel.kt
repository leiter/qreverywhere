package cut.the.crap.qreverywhere.qrcodecreate

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.utils.ProtocolPrefix.TEL
import cut.the.crap.qreverywhere.utils.data.EncryptedPrefs
import cut.the.crap.qreverywhere.utils.data.SingleLiveDataEvent
import cut.the.crap.qreverywhere.utils.textToImageEnc
import cut.the.crap.qrrepository.Acquire
import cut.the.crap.qrrepository.QrItem
import cut.the.crap.qrrepository.db.toItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateOneLinerViewModel @Inject constructor(
    private val historyRepository: cut.the.crap.qrrepository.QrHistoryRepository,
    private val encryptedPrefs: EncryptedPrefs
) : ViewModel() {

    val qrCodeItemState = SingleLiveDataEvent<State<QrItem>>(null)

    var currentInputText = ""
    var currentInputNumber = ""

    private fun createWebQrcode(resources: Resources, activityViewModel: MainActivityViewModel) {
        viewModelScope.launch { createCQrcode(resources, activityViewModel,currentInputText) }
    }

    private fun createTextQrcode(resources: Resources, activityViewModel: MainActivityViewModel) {
        viewModelScope.launch { createCQrcode(resources, activityViewModel,currentInputText) }
    }

    private fun createCallQrcode(resources: Resources, activityViewModel: MainActivityViewModel) {
        viewModelScope.launch {
            val uriString = TEL + currentInputNumber
            createCQrcode(resources,activityViewModel, uriString)
        }
    }

    private fun createCQrcode(resources: Resources, activityViewModel: MainActivityViewModel, text: String) {
        viewModelScope.launch {
            val qrCodeItem = createQrItem(text)
            activityViewModel.setDetailViewItem(qrCodeItem)
            qrCodeItemState.value = State.success()
            historyRepository.insertQrItem(qrCodeItem)
        }

    }

    private suspend fun createQrItem(text: String) : QrItem {
        val bitmap = textToImageEnc(text, encryptedPrefs.foregroundColor, encryptedPrefs.backgroundColor)
        return cut.the.crap.qrrepository.db.QrCodeDbItem(img = bitmap, textContent = text, acquireType = Acquire.CREATED).toItem()
    }

    private fun testWebQrcode() {
        val isValid = isValidWebUrl()
        if (isValid) {
            viewModelScope.launch {
                val qrCodeItem = createQrItem(currentInputText)
                qrCodeItemState.value = State.success(qrCodeItem)
            }
        } else {
            qrCodeItemState.value = State.error(error = InvalidWebUrl())
        }
    }

    private fun testCallQrcode(): Boolean {
        val isValid = isValidPhoneNumber()
        if (isValid) {
            viewModelScope.launch {
                val uriString = TEL + currentInputNumber
                val qrCodeItem = createQrItem(uriString)
                qrCodeItemState.value = State.success(qrCodeItem)
            }
        } else {
            qrCodeItemState.value = State.error(error = InvalidPhoneNumber())
        }
        return isValid
    }

    private fun isValidPhoneNumber(): Boolean {
        return android.util.Patterns.PHONE.matcher(currentInputNumber).matches()
    }

    private fun isValidWebUrl(): Boolean {
        currentInputText = currentInputText.trim()
        return android.util.Patterns.WEB_URL.matcher(currentInputText).matches()
    }

    private fun startLoading() {
        qrCodeItemState.value = State.loading()
    }

    fun createClicked(mode: Int, resources: Resources, activityViewModel: MainActivityViewModel) {
//        startLoading()
        when (mode) {
            CreateOneLinerFragment.CREATE_PHONE -> {
                if (isValidPhoneNumber()) createCallQrcode(resources, activityViewModel)
                else qrCodeItemState.value = State.error(error = InvalidPhoneNumber())
            }
            CreateOneLinerFragment.CREATE_SMS -> {
                if (currentInputText.isNotEmpty()) createTextQrcode(resources, activityViewModel)
                else qrCodeItemState.value = State.error(error = EmptyMessage())
            }
            CreateOneLinerFragment.CREATE_WEB -> {
                if (isValidWebUrl()) createWebQrcode(resources, activityViewModel)
                else qrCodeItemState.value = State.error(error = InvalidWebUrl())
            }

            else -> throw IllegalArgumentException("No function associated with mode $mode")
        }
    }

    fun testClicked(mode: Int) {
        startLoading()
        when (mode) {
            CreateOneLinerFragment.CREATE_PHONE -> testCallQrcode()
            CreateOneLinerFragment.CREATE_WEB -> testWebQrcode()
            else -> throw IllegalArgumentException("No function associated with mode $mode")
        }
    }

}

class EmptyMessage : Exception()
class InvalidPhoneNumber : Exception()
class InvalidWebUrl : Exception()
