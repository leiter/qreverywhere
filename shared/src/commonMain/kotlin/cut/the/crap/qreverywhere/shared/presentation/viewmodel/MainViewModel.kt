package cut.the.crap.qreverywhere.shared.presentation.viewmodel

import cut.the.crap.qreverywhere.shared.domain.model.QrItem
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for KMP
 * This replaces MainActivityViewModel in Android
 */
class MainViewModel(
    private val qrRepository: QrRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // State for QR history
    private val _historyData = MutableStateFlow<List<QrItem>>(emptyList())
    val historyData: StateFlow<List<QrItem>> = _historyData.asStateFlow()

    // State for detail view
    private val _detailViewItem = MutableStateFlow<QrItem?>(null)
    val detailViewItem: StateFlow<QrItem?> = _detailViewItem.asStateFlow()

    // Events
    private val _saveQrImageEvent = MutableSharedFlow<SaveEvent>()
    val saveQrImageEvent: SharedFlow<SaveEvent> = _saveQrImageEvent.asSharedFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            qrRepository.getQrHistory().collect { items ->
                _historyData.value = items
                Logger.d("MainViewModel") { "Loaded ${items.size} QR items from history" }
            }
        }
    }

    fun setDetailViewItem(item: QrItem) {
        _detailViewItem.value = item
        Logger.d("MainViewModel") { "Set detail view item: ${item.id}" }
    }

    fun saveQrItem(item: QrItem) {
        viewModelScope.launch {
            try {
                qrRepository.insertQrItem(item)
                _saveQrImageEvent.emit(SaveEvent.Success)
                Logger.d("MainViewModel") { "Saved QR item: ${item.id}" }
            } catch (e: Exception) {
                _saveQrImageEvent.emit(SaveEvent.Error(e.message ?: "Unknown error"))
                Logger.e("MainViewModel", e) { "Failed to save QR item" }
            }
        }
    }

    fun deleteQrItem(item: QrItem) {
        viewModelScope.launch {
            try {
                qrRepository.deleteQrItem(item)
                Logger.d("MainViewModel") { "Deleted QR item: ${item.id}" }
            } catch (e: Exception) {
                Logger.e("MainViewModel", e) { "Failed to delete QR item" }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                qrRepository.deleteAll()
                Logger.d("MainViewModel") { "Cleared QR history" }
            } catch (e: Exception) {
                Logger.e("MainViewModel", e) { "Failed to clear history" }
            }
        }
    }

    sealed class SaveEvent {
        object Success : SaveEvent()
        data class Error(val message: String) : SaveEvent()
    }
}