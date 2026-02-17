package cut.the.crap.qreverywhere.shared.presentation.viewmodel

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

/**
 * Shared ViewModel for KMP using androidx.lifecycle.ViewModel
 * This replaces MainActivityViewModel in Android and will be used by iOS
 */
class MainViewModel(
    private val qrRepository: QrRepository,
    private val qrCodeGenerator: QrCodeGenerator,
    private val saveImageUseCase: SaveImageToFileUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // State for QR history
    private val _historyData = MutableStateFlow<List<QrItem>>(emptyList())
    val historyData: StateFlow<List<QrItem>> = _historyData.asStateFlow()

    // Search query for filtering history
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered history based on search query
    private val _filteredHistoryData = MutableStateFlow<List<QrItem>>(emptyList())
    val filteredHistoryData: StateFlow<List<QrItem>> = _filteredHistoryData.asStateFlow()

    // State for detail view item
    private val _detailViewItem = MutableStateFlow<QrItem?>(null)
    val detailViewItem: StateFlow<QrItem?> = _detailViewItem.asStateFlow()

    // State for detail view with loading/error states
    private val _detailViewState = MutableStateFlow<State<QrItem>?>(null)
    val detailViewState: StateFlow<State<QrItem>?> = _detailViewState.asStateFlow()

    // Events for saving QR images to file
    private val _saveQrImageEvent = MutableSharedFlow<State<String?>>()
    val saveQrImageEvent: SharedFlow<State<String?>> = _saveQrImageEvent.asSharedFlow()

    // Transient error events for snackbar display
    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    // Clipboard content for creating QR codes from clipboard (desktop)
    private val _clipboardContent = MutableStateFlow<String?>(null)
    val clipboardContent: StateFlow<String?> = _clipboardContent.asStateFlow()

    // Undo delete support - track last deleted item
    private val _lastDeletedItem = MutableStateFlow<QrItem?>(null)
    val lastDeletedItem: StateFlow<QrItem?> = _lastDeletedItem.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            qrRepository.getQrHistory().collect { items ->
                _historyData.value = items
                applySearchFilter(items, _searchQuery.value)
                Logger.d("MainViewModel") { "Loaded ${items.size} QR items from history" }
            }
        }
    }

    /**
     * Update search query and filter history
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applySearchFilter(_historyData.value, query)
    }

    /**
     * Clear search query
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _filteredHistoryData.value = _historyData.value
    }

    /**
     * Apply search filter to history items
     */
    private fun applySearchFilter(items: List<QrItem>, query: String) {
        _filteredHistoryData.value = if (query.isBlank()) {
            items
        } else {
            val lowercaseQuery = query.lowercase()
            items.filter { item ->
                item.textContent.lowercase().contains(lowercaseQuery) ||
                item.acquireType.name.lowercase().contains(lowercaseQuery)
            }
        }
    }

    /**
     * Set the current detail view item
     */
    fun setDetailViewItem(item: QrItem) {
        _detailViewItem.value = item
        Logger.d("MainViewModel") { "Set detail view item: ${item.id}" }
    }

    /**
     * Save a QR item to the repository
     */
    fun saveQrItem(item: QrItem) {
        viewModelScope.launch {
            try {
                qrRepository.insertQrItem(item)
                Logger.d("MainViewModel") { "Saved QR item: ${item.id}" }
            } catch (e: Exception) {
                val message = ErrorHandler.getDisplayMessage(e)
                _errorEvent.emit(message)
                Logger.e("MainViewModel", e) { "Failed to save QR item" }
            }
        }
    }

    /**
     * Create and save a QR item from text content
     * This generates the QR code image using user's color preferences
     */
    fun saveQrItemFromText(textContent: String, acquireType: AcquireType) {
        if (acquireType == AcquireType.FROM_FILE) {
            _detailViewState.value = State.loading()
        }

        viewModelScope.launch {
            try {
                // Generate QR code image with user preferences
                val imageData = qrCodeGenerator.generateQrCode(
                    text = textContent,
                    foregroundColor = userPreferences.getForegroundColor(),
                    backgroundColor = userPreferences.getBackgroundColor()
                )

                // Create QR item
                val qrItem = QrItem(
                    id = 0, // Will be auto-generated by database
                    textContent = textContent,
                    acquireType = acquireType,
                    timestamp = Clock.System.now(),
                    imageData = imageData
                )

                // Save to repository
                qrRepository.insertQrItem(qrItem)

                // Update detail view
                _detailViewItem.value = qrItem
                _detailViewState.value = State.success(qrItem)

                Logger.d("MainViewModel") { "Created and saved QR item from text" }
            } catch (e: Exception) {
                val message = ErrorHandler.getDisplayMessage(e)
                _detailViewState.value = State.error(message, throwable = e)
                _errorEvent.emit(message)
                Logger.e("MainViewModel", e) { "Failed to create QR item from text" }
            }
        }
    }

    /**
     * Save the current detail view QR image to device storage
     */
    fun saveQrImageOfDetailView() {
        val currentItem = _detailViewItem.value
        if (currentItem == null) {
            Logger.w("MainViewModel") { "No detail view item to save" }
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

                // Save image to file
                val filePath = saveImageUseCase.saveImage(imageData)

                if (filePath != null) {
                    _saveQrImageEvent.emit(State.success(filePath))
                    Logger.d("MainViewModel") { "Saved QR image to: $filePath" }
                } else {
                    _saveQrImageEvent.emit(State.error("Failed to save image"))
                    Logger.e("MainViewModel") { "Failed to save QR image" }
                }
            } catch (e: Exception) {
                val message = ErrorHandler.getDisplayMessage(e)
                _saveQrImageEvent.emit(State.error(message, throwable = e))
                Logger.e("MainViewModel", e) { "Error saving QR image" }
            }
        }
    }

    /**
     * Delete the current detail view item
     */
    fun deleteCurrentDetailView() {
        val currentItem = _detailViewItem.value
        if (currentItem != null) {
            deleteQrItem(currentItem)
        } else {
            Logger.w("MainViewModel") { "No detail view item to delete" }
        }
    }

    /**
     * Delete a QR item from the repository
     * Stores the item for potential undo
     */
    fun deleteQrItem(item: QrItem) {
        viewModelScope.launch {
            try {
                // Store for undo before deleting
                _lastDeletedItem.value = item
                qrRepository.deleteQrItem(item)
                Logger.d("MainViewModel") { "Deleted QR item: ${item.id}" }
            } catch (e: Exception) {
                _lastDeletedItem.value = null
                val message = ErrorHandler.getDisplayMessage(e)
                _errorEvent.emit(message)
                Logger.e("MainViewModel", e) { "Failed to delete QR item" }
            }
        }
    }

    /**
     * Restore the last deleted item (undo)
     */
    fun undoDelete() {
        val deletedItem = _lastDeletedItem.value ?: return
        viewModelScope.launch {
            try {
                qrRepository.insertQrItem(deletedItem)
                _lastDeletedItem.value = null
                Logger.d("MainViewModel") { "Restored deleted QR item: ${deletedItem.id}" }
            } catch (e: Exception) {
                val message = ErrorHandler.getDisplayMessage(e)
                _errorEvent.emit(message)
                Logger.e("MainViewModel", e) { "Failed to restore QR item" }
            }
        }
    }

    /**
     * Clear the last deleted item (when undo window expires)
     */
    fun clearLastDeletedItem() {
        _lastDeletedItem.value = null
    }

    /**
     * Remove a history item at the specified position
     */
    fun removeHistoryItem(position: Int) {
        if (position < 0) return

        viewModelScope.launch {
            try {
                val items = _historyData.value
                if (position < items.size) {
                    qrRepository.deleteQrItem(items[position])
                    Logger.d("MainViewModel") { "Deleted QR item at position $position" }
                }
            } catch (e: Exception) {
                val message = ErrorHandler.getDisplayMessage(e)
                _errorEvent.emit(message)
                Logger.e("MainViewModel", e) { "Failed to remove history item" }
            }
        }
    }

    /**
     * Clear all QR history
     */
    fun clearHistory() {
        viewModelScope.launch {
            try {
                qrRepository.deleteAll()
                Logger.d("MainViewModel") { "Cleared QR history" }
            } catch (e: Exception) {
                val message = ErrorHandler.getDisplayMessage(e)
                _errorEvent.emit(message)
                Logger.e("MainViewModel", e) { "Failed to clear history" }
            }
        }
    }

    /**
     * Set clipboard content for creating QR codes (used by desktop tray)
     */
    fun setClipboardContent(content: String) {
        _clipboardContent.value = content
        Logger.d("MainViewModel") { "Set clipboard content for QR creation" }
    }

    /**
     * Clear clipboard content after it's been used
     */
    fun clearClipboardContent() {
        _clipboardContent.value = null
    }
}