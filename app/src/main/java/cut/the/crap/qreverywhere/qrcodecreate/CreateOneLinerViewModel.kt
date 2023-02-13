package cut.the.crap.qreverywhere.qrcodecreate

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qreverywhere.db.QrCodeItem
import cut.the.crap.qreverywhere.repository.QrHistoryRepository
import cut.the.crap.qreverywhere.utils.Acquire
import cut.the.crap.qreverywhere.utils.ContactManager
import cut.the.crap.qreverywhere.utils.SingleLiveDataEvent
import cut.the.crap.qreverywhere.utils.textToImageEnc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateOneLinerViewModel @Inject constructor(
    private val historyRepository: QrHistoryRepository
) : ViewModel() {

    val qrCodeItemState = SingleLiveDataEvent<State<QrCodeItem>>(null)

    var currentInputText = ""
    var currentInputNumber = ""

    private fun createWebQrcode(resources: Resources, activityViewModel: MainActivityViewModel) {
        viewModelScope.launch {
            val bitmap = textToImageEnc(currentInputText, resources)
            val qrCodeItem = QrCodeItem(
                img = bitmap,
                textContent = currentInputText,
                acquireType = Acquire.CREATED
            )
            historyRepository.insertQrItem(qrCodeItem)
            activityViewModel.detailViewQrCodeItem = qrCodeItem
            qrCodeItemState.value = State.success()
        }
    }

    private fun createTextQrcode(resources: Resources, activityViewModel: MainActivityViewModel) {
        viewModelScope.launch {
            val bitmap = textToImageEnc(currentInputText, resources)
            val qrCodeItem =
                QrCodeItem(
                    img = bitmap,
                    textContent = currentInputText,
                    acquireType = Acquire.CREATED
                )
            historyRepository.insertQrItem(qrCodeItem)
            activityViewModel.detailViewQrCodeItem = qrCodeItem
            qrCodeItemState.value = State.success()
        }
    }

    private fun createCallQrcode(resources: Resources, activityViewModel: MainActivityViewModel) {
        viewModelScope.launch {
            val uriString = TEL_PREFIX + currentInputNumber
            val bitmap = textToImageEnc(uriString, resources)
            val qrCodeItem =
                QrCodeItem(img = bitmap, textContent = uriString, acquireType = Acquire.CREATED)
            historyRepository.insertQrItem(qrCodeItem)
            activityViewModel.detailViewQrCodeItem = qrCodeItem
            qrCodeItemState.value = State.success()
        }

    }

    private fun testWebQrcode(resources: Resources) {
        val isValid = isValidWebUrl()
        if (isValid) {
            viewModelScope.launch {
                val bitmap = textToImageEnc(currentInputText, resources)
                val qrCodeItem = QrCodeItem(
                    img = bitmap,
                    textContent = currentInputText,
                    acquireType = Acquire.CREATED
                )
                qrCodeItemState.value = State.success(qrCodeItem)
            }
        } else {
            qrCodeItemState.value = State.error(error = InvalidWebUrl())
        }
    }

    private fun testSMSQrcode(resources: Resources) {
        val isValid = isValidPhoneNumber()
        if (isValid) {
            viewModelScope.launch {
                val uriString = SMS_PREFIX + currentInputNumber
                val bitmap = textToImageEnc(uriString, resources)
                val qrCodeItem =
                    QrCodeItem(img = bitmap, textContent = uriString, acquireType = Acquire.CREATED)
                qrCodeItemState.value = State.success(qrCodeItem)
            }
        } else {
            qrCodeItemState.value = State.error(error = InvalidPhoneNumber())
        }
    }

    private fun testCallQrcode(resources: Resources): Boolean {
        val isValid = isValidPhoneNumber()
        if (isValid) {
            viewModelScope.launch {
                val uriString = TEL_PREFIX + currentInputNumber
                val bitmap = textToImageEnc(uriString, resources)
                val qrCodeItem =
                    QrCodeItem(img = bitmap, textContent = uriString, acquireType = Acquire.CREATED)
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
        startLoading()
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

    fun testClicked(mode: Int, resources: Resources) {



        startLoading()
        when (mode) {
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

class EmptyMessage : Exception()
class NoTextInput : Exception()
class InvalidPhoneNumber : Exception()
class InvalidWebUrl : Exception()
