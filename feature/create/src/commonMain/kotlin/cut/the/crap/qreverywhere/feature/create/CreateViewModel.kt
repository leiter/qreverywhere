package cut.the.crap.qreverywhere.feature.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import cut.the.crap.qreverywhere.shared.domain.model.QrAction
import cut.the.crap.qreverywhere.shared.domain.model.QrItem
import cut.the.crap.qreverywhere.shared.domain.model.toQrAction
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.domain.usecase.QrActionLauncher
import cut.the.crap.qreverywhere.shared.domain.usecase.QrActionResult
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import cut.the.crap.qreverywhere.shared.utils.ErrorHandler
import cut.the.crap.qreverywhere.shared.utils.Logger
import kotlinx.coroutines.launch
import kotlin.time.Clock

class CreateViewModel(
    private val qrRepository: QrRepository,
    private val qrCodeGenerator: QrCodeGenerator,
    private val userPreferences: UserPreferences,
    private val qrActionLauncher: QrActionLauncher
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

    /**
     * Resolves the given in-progress creation content string to the [QrAction] it
     * represents, used to drive the "Test" button on creation screens. Pure resolution -
     * no DB write, no navigation.
     */
    fun resolveTestAction(content: String): QrAction = content.toQrAction()

    /**
     * Executes the given [action] via the platform [QrActionLauncher]. Pure passthrough -
     * the UI layer owns Snackbar/dialog/card decision-making, this just performs the action
     * once the user has confirmed it. Does not save the content or navigate away.
     */
    fun executeTestAction(action: QrAction): QrActionResult = qrActionLauncher.launch(action)
}
