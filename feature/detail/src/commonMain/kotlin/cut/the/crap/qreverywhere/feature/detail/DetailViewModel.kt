package cut.the.crap.qreverywhere.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import cut.the.crap.qreverywhere.shared.domain.model.QrItem
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.SaveImageToFileUseCase
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import cut.the.crap.qreverywhere.shared.presentation.state.State
import cut.the.crap.qreverywhere.shared.utils.ErrorHandler
import cut.the.crap.qreverywhere.shared.utils.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class DetailViewModel(
    private val qrRepository: QrRepository,
    private val saveImageUseCase: SaveImageToFileUseCase,
    private val qrCodeGenerator: QrCodeGenerator,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _detailViewItem = MutableStateFlow<QrItem?>(null)
    val detailViewItem: StateFlow<QrItem?> = _detailViewItem.asStateFlow()

    private val _detailViewState = MutableStateFlow<State<QrItem>?>(null)
    val detailViewState: StateFlow<State<QrItem>?> = _detailViewState.asStateFlow()

    private val _saveQrImageEvent = MutableSharedFlow<State<String?>>()
    val saveQrImageEvent: SharedFlow<State<String?>> = _saveQrImageEvent.asSharedFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    private val _lastDeletedItem = MutableStateFlow<QrItem?>(null)
    val lastDeletedItem: StateFlow<QrItem?> = _lastDeletedItem.asStateFlow()

    fun setDetailViewItem(item: QrItem) {
        _detailViewItem.value = item
        _detailViewState.value = State.success(item)
        Logger.d("DetailViewModel") { "Set detail view item: ${item.id}" }
    }

    fun saveScannedQrItem(
        textContent: String,
        acquireType: AcquireType,
        onResult: (Result<QrItem>) -> Unit
    ) {
        if (acquireType == AcquireType.FROM_FILE) {
            _detailViewState.value = State.loading()
        }

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
                _detailViewItem.value = qrItem
                _detailViewState.value = State.success(qrItem)

                Logger.d("DetailViewModel") { "Saved scanned QR item" }
                onResult(Result.success(qrItem))
            } catch (e: Exception) {
                val message = ErrorHandler.getDisplayMessage(e)
                _detailViewState.value = State.error(message, throwable = e)
                _errorEvent.emit(message)
                Logger.e("DetailViewModel", e) { "Failed to save scanned QR item" }
                onResult(Result.failure(e))
            }
        }
    }

    fun saveQrImageOfDetailView() {
        val currentItem = _detailViewItem.value
        if (currentItem == null) {
            Logger.w("DetailViewModel") { "No detail view item to save" }
            return
        }

        viewModelScope.launch {
            try {
                _saveQrImageEvent.emit(State.loading())

                val imageData = currentItem.imageData
                if (imageData == null || imageData.isEmpty()) {
                    _saveQrImageEvent.emit(State.error("No image data available"))
                    return@launch
                }

                val filePath = saveImageUseCase.saveImage(imageData)

                if (filePath != null) {
                    _saveQrImageEvent.emit(State.success(filePath))
                    Logger.d("DetailViewModel") { "Saved QR image to: $filePath" }
                } else {
                    _saveQrImageEvent.emit(State.error("Failed to save image"))
                    Logger.e("DetailViewModel") { "Failed to save QR image" }
                }
            } catch (e: Exception) {
                val message = ErrorHandler.getDisplayMessage(e)
                _saveQrImageEvent.emit(State.error(message, throwable = e))
                Logger.e("DetailViewModel", e) { "Error saving QR image" }
            }
        }
    }

    fun deleteCurrentDetailView() {
        val currentItem = _detailViewItem.value
        if (currentItem != null) {
            _lastDeletedItem.value = currentItem
            deleteQrItem(currentItem)
        } else {
            Logger.w("DetailViewModel") { "No detail view item to delete" }
        }
    }

    fun undoDelete() {
        val deletedItem = _lastDeletedItem.value ?: return
        viewModelScope.launch {
            try {
                qrRepository.insertQrItem(deletedItem)
                _lastDeletedItem.value = null
                Logger.d("DetailViewModel") { "Undo delete: re-inserted QR item ${deletedItem.id}" }
            } catch (e: Exception) {
                val message = ErrorHandler.getDisplayMessage(e)
                _errorEvent.emit(message)
                Logger.e("DetailViewModel", e) { "Failed to undo delete" }
            }
        }
    }

    fun clearLastDeletedItem() {
        _lastDeletedItem.value = null
    }

    fun deleteQrItem(item: QrItem) {
        viewModelScope.launch {
            try {
                qrRepository.deleteQrItem(item)
                Logger.d("DetailViewModel") { "Deleted QR item: ${item.id}" }
            } catch (e: Exception) {
                val message = ErrorHandler.getDisplayMessage(e)
                _errorEvent.emit(message)
                Logger.e("DetailViewModel", e) { "Failed to delete QR item" }
            }
        }
    }
}
