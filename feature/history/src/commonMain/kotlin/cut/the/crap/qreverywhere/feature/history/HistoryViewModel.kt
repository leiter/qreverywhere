package cut.the.crap.qreverywhere.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cut.the.crap.qreverywhere.shared.domain.model.QrItem
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.utils.ErrorHandler
import cut.the.crap.qreverywhere.shared.utils.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val qrRepository: QrRepository
) : ViewModel() {

    private val _historyData = MutableStateFlow<List<QrItem>>(emptyList())
    val historyData: StateFlow<List<QrItem>> = _historyData.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredHistoryData = MutableStateFlow<List<QrItem>>(emptyList())
    val filteredHistoryData: StateFlow<List<QrItem>> = _filteredHistoryData.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            qrRepository.getQrHistory().collect { items ->
                _historyData.value = items
                applySearchFilter()
                Logger.d("HistoryViewModel") { "Loaded ${items.size} QR items from history" }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applySearchFilter()
    }

    fun clearSearch() {
        _searchQuery.value = ""
        applySearchFilter()
    }

    private fun applySearchFilter() {
        val query = _searchQuery.value.trim().lowercase()
        val items = _historyData.value
        _filteredHistoryData.value = if (query.isEmpty()) {
            items
        } else {
            items.filter { item ->
                item.textContent.lowercase().contains(query) ||
                    item.acquireType.name.lowercase().contains(query)
            }
        }
    }

    fun removeHistoryItem(position: Int) {
        if (position < 0) return

        viewModelScope.launch {
            try {
                val items = _historyData.value
                if (position < items.size) {
                    qrRepository.deleteQrItem(items[position])
                    Logger.d("HistoryViewModel") { "Deleted QR item at position $position" }
                }
            } catch (e: Exception) {
                val message = ErrorHandler.getDisplayMessage(e)
                _errorEvent.emit(message)
                Logger.e("HistoryViewModel", e) { "Failed to remove history item" }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                qrRepository.deleteAll()
                Logger.d("HistoryViewModel") { "Cleared QR history" }
            } catch (e: Exception) {
                val message = ErrorHandler.getDisplayMessage(e)
                _errorEvent.emit(message)
                Logger.e("HistoryViewModel", e) { "Failed to clear history" }
            }
        }
    }
}
