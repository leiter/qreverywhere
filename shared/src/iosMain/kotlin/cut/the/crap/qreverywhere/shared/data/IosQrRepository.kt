package cut.the.crap.qreverywhere.shared.data

import cut.the.crap.qreverywhere.shared.domain.model.QrItem
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * iOS implementation of QrRepository
 * For now, this is a simple in-memory implementation
 * TODO: Replace with Core Data or SQLDelight implementation
 */
class IosQrRepository : QrRepository {

    private val qrItems = mutableListOf<QrItem>()
    private val _historyFlow = MutableStateFlow<List<QrItem>>(emptyList())

    override fun getQrHistory(): Flow<List<QrItem>> {
        return _historyFlow.asStateFlow()
    }

    override suspend fun insertQrItem(qrItem: QrItem) {
        val newItem = if (qrItem.id == 0) {
            qrItem.copy(id = (qrItems.maxOfOrNull { it.id } ?: 0) + 1)
        } else {
            qrItem
        }
        qrItems.add(newItem)
        _historyFlow.value = qrItems.toList()
    }

    override suspend fun deleteQrItem(qrItem: QrItem) {
        qrItems.removeAll { it.id == qrItem.id }
        _historyFlow.value = qrItems.toList()
    }

    override suspend fun deleteAll() {
        qrItems.clear()
        _historyFlow.value = emptyList()
    }

    override suspend fun getQrItem(id: Int): QrItem? {
        return qrItems.find { it.id == id }
    }

    override suspend fun updateQrItem(qrItem: QrItem) {
        val index = qrItems.indexOfFirst { it.id == qrItem.id }
        if (index >= 0) {
            qrItems[index] = qrItem
            _historyFlow.value = qrItems.toList()
        }
    }
}