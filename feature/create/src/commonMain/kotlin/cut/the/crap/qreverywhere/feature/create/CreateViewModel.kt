package cut.the.crap.qreverywhere.feature.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import cut.the.crap.qreverywhere.shared.domain.model.QrItem
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import cut.the.crap.qreverywhere.shared.utils.ErrorHandler
import cut.the.crap.qreverywhere.shared.utils.Logger
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class CreateViewModel(
    private val qrRepository: QrRepository,
    private val qrCodeGenerator: QrCodeGenerator,
    private val userPreferences: UserPreferences
) : ViewModel() {

    /**
     * Create and save a QR item from text content.
     * Returns the created QrItem on success via the callback.
     */
    fun createQrItem(
        textContent: String,
        acquireType: AcquireType,
        onResult: (Result<QrItem>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val imageData = qrCodeGenerator.generateQrCode(
                    text = textContent,
                    foregroundColor = userPreferences.getForegroundColor(),
                    backgroundColor = userPreferences.getBackgroundColor()
                )

                val qrItem = QrItem(
                    id = 0,
                    textContent = textContent,
                    acquireType = acquireType,
                    timestamp = Clock.System.now(),
                    imageData = imageData
                )

                qrRepository.insertQrItem(qrItem)

                Logger.d("CreateViewModel") { "Created and saved QR item from text" }
                onResult(Result.success(qrItem))
            } catch (e: Exception) {
                Logger.e("CreateViewModel", e) { "Failed to create QR item from text" }
                onResult(Result.failure(e))
            }
        }
    }
}
