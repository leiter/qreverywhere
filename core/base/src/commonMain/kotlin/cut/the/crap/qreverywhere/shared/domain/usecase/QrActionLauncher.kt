package cut.the.crap.qreverywhere.shared.domain.usecase

import cut.the.crap.qreverywhere.shared.domain.model.QrAction

/**
 * Platform-specific use case for triggering the concrete system action a [QrAction]
 * represents (dial a number, open a URL, send an email, etc.), used to drive the
 * "Test"/"Open" action feature on Detail and Create screens.
 */
interface QrActionLauncher {
    /**
     * Launches the given [action] using the appropriate platform mechanism.
     * @return [QrActionResult.Success] if the platform handler was launched,
     * [QrActionResult.NoHandlerAvailable] if this platform has no handler for the
     * action (yet), or [QrActionResult.Failed] if launching failed.
     */
    fun launch(action: QrAction): QrActionResult
}

/**
 * Outcome of attempting to launch a [QrAction] via [QrActionLauncher.launch].
 */
sealed class QrActionResult {
    data object Success : QrActionResult()
    data object NoHandlerAvailable : QrActionResult()
    data class Failed(val reason: String) : QrActionResult()
}
