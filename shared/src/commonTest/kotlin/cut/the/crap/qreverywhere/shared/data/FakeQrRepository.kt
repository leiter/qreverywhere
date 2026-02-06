package cut.the.crap.qreverywhere.shared.data

import cut.the.crap.qreverywhere.shared.domain.model.QrItem
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of QrRepository for testing
 */
class FakeQrRepository : QrRepository {
    private val items = MutableStateFlow<MutableList<QrItem>>(mutableListOf())
    private var nextId = 1

    // For testing purposes
    var shouldThrowOnInsert = false
    var shouldThrowOnDelete = false
    var insertException: Exception = RuntimeException("Insert failed")
    var deleteException: Exception = RuntimeException("Delete failed")

    override fun getQrHistory(): Flow<List<QrItem>> {
        return items.map { it.toList().sortedByDescending { item -> item.timestamp } }
    }

    override suspend fun insertQrItem(qrItem: QrItem) {
        if (shouldThrowOnInsert) {
            throw insertException
        }
        val newItem = if (qrItem.id == 0) {
            qrItem.copy(id = nextId++)
        } else {
            qrItem
        }
        val currentList = items.value.toMutableList()
        currentList.add(newItem)
        items.value = currentList
    }

    override suspend fun deleteQrItem(qrItem: QrItem) {
        if (shouldThrowOnDelete) {
            throw deleteException
        }
        val currentList = items.value.toMutableList()
        currentList.removeAll { it.id == qrItem.id }
        items.value = currentList
    }

    override suspend fun deleteAll() {
        items.value = mutableListOf()
    }

    override suspend fun getQrItem(id: Int): QrItem? {
        return items.value.find { it.id == id }
    }

    override suspend fun updateQrItem(qrItem: QrItem) {
        val currentList = items.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == qrItem.id }
        if (index >= 0) {
            currentList[index] = qrItem
            items.value = currentList
        }
    }

    // Test helper methods
    fun getItemsSnapshot(): List<QrItem> = items.value.toList()

    fun clear() {
        items.value = mutableListOf()
        nextId = 1
        shouldThrowOnInsert = false
        shouldThrowOnDelete = false
    }
}
