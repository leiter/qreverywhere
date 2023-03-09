package cut.the.crap.qreverywhere.qrcodecreate

import android.content.res.Resources
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.WriterException
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.data.State
import cut.the.crap.qrrepository.Acquire
import cut.the.crap.qreverywhere.utils.textToImageEnc
import cut.the.crap.qrrepository.QrItem
import cut.the.crap.qrrepository.db.toItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateQrCodeViewModel @Inject constructor(
    private val qrHistoryRepository: cut.the.crap.qrrepository.QrHistoryRepository
) : ViewModel() {

    val emailQrCodeItem = MutableLiveData<State<QrItem>>()

    var emailAddress = ""
    var emailSubject = ""
    var emailText = ""

    @Throws(WriterException::class)
    fun textToQrCodeItem(resources: Resources, activityViewModel: MainActivityViewModel) {
        viewModelScope.launch {
            var qrItem: cut.the.crap.qrrepository.db.QrCodeDbItem? = null
            var saveInHistory = false
            try {
                emailQrCodeItem.value = State.loading()
                checkValidEmail(emailAddress)
                val textContent =
                    "mailto:%s?subject=%s&body=%s".format(
                        Uri.encode(emailAddress),
                        Uri.encode(emailSubject),
                        Uri.encode(emailText)
                    )

                val bitmap = textToImageEnc(textContent, resources)
                qrItem = cut.the.crap.qrrepository.db.QrCodeDbItem(
                    img = bitmap,
                    textContent = textContent,
                    acquireType = Acquire.CREATED
                )
                emailQrCodeItem.value = State.success(qrItem.toItem())
                activityViewModel.setDetailViewItem(qrItem.toItem())
                saveInHistory = true
            } catch (e: InvalidEmailException) {
                emailQrCodeItem.value = State.error(error = e)
            }

            if(saveInHistory){

                try {
                    qrItem?.let {
                        qrHistoryRepository.insertQrItem(it.toItem())
                        emailQrCodeItem.value = State.success()
                        emailQrCodeItem.value = State.success(qrItem.toItem())
                    }

                } catch (e: Exception) {
                    emailQrCodeItem.value = State.error(error = NotSavedToHistoryException())
                }
            }
        }
    }

    @Throws(InvalidEmailException::class)
    private fun checkValidEmail(emailAddress: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
            throw InvalidEmailException()
        }
    }

}

class InvalidEmailException : Exception()

class NotSavedToHistoryException : Exception()

