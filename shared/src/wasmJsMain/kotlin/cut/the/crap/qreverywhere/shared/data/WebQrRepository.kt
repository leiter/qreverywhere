package cut.the.crap.qreverywhere.shared.data

import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import cut.the.crap.qreverywhere.shared.domain.model.QrItem
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant

/**
 * Web implementation of QrRepository using localStorage
 * Uses JSON serialization for persistence
 */
class WebQrRepository : QrRepository {

    private val _historyFlow = MutableStateFlow<List<QrItem>>(emptyList())
    private var nextId = 1

    init {
        loadFromStorage()
    }

    override fun getQrHistory(): Flow<List<QrItem>> = _historyFlow.asStateFlow()

    override suspend fun insertQrItem(qrItem: QrItem) {
        val newItem = qrItem.copy(id = nextId++)
        val current = _historyFlow.value.toMutableList()
        current.add(0, newItem) // Add at beginning (newest first)
        _historyFlow.value = current
        saveToStorage()
    }

    override suspend fun deleteQrItem(qrItem: QrItem) {
        val current = _historyFlow.value.toMutableList()
        current.removeAll { it.id == qrItem.id }
        _historyFlow.value = current
        saveToStorage()
    }

    override suspend fun deleteAll() {
        _historyFlow.value = emptyList()
        saveToStorage()
    }

    override suspend fun getQrItem(id: Int): QrItem? {
        return _historyFlow.value.find { it.id == id }
    }

    override suspend fun updateQrItem(qrItem: QrItem) {
        val current = _historyFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == qrItem.id }
        if (index >= 0) {
            current[index] = qrItem
            _historyFlow.value = current
            saveToStorage()
        }
    }

    private fun loadFromStorage() {
        try {
            val json = localStorageGetItem(STORAGE_KEY) ?: return
            val items = parseQrItemsJson(json)
            _historyFlow.value = items
            nextId = (items.maxOfOrNull { it.id } ?: 0) + 1
        } catch (e: Exception) {
            // Start with empty list on error
            _historyFlow.value = emptyList()
        }
    }

    private fun saveToStorage() {
        try {
            val json = serializeQrItems(_historyFlow.value)
            localStorageSetItem(STORAGE_KEY, json)
        } catch (e: Exception) {
            // Ignore save errors
        }
    }

    private fun serializeQrItems(items: List<QrItem>): String {
        return buildString {
            append("[")
            items.forEachIndexed { index, item ->
                if (index > 0) append(",")
                append("{")
                append("\"id\":${item.id},")
                append("\"textContent\":\"${escapeJson(item.textContent)}\",")
                append("\"acquireType\":\"${item.acquireType.name}\",")
                append("\"timestamp\":${item.timestamp.toEpochMilliseconds()}")
                // Note: imageData is not persisted to save space
                append("}")
            }
            append("]")
        }
    }

    private fun parseQrItemsJson(json: String): List<QrItem> {
        val items = mutableListOf<QrItem>()
        try {
            // Simple JSON parsing for our known structure
            val itemsJson = parseJsonArray(json)
            for (itemJson in itemsJson) {
                val id = extractJsonInt(itemJson, "id") ?: continue
                val textContent = extractJsonString(itemJson, "textContent") ?: continue
                val acquireTypeName = extractJsonString(itemJson, "acquireType") ?: "CREATED"
                val timestampMs = extractJsonLong(itemJson, "timestamp") ?: 0L

                val acquireType = try {
                    AcquireType.valueOf(acquireTypeName)
                } catch (e: Exception) {
                    AcquireType.CREATED
                }

                items.add(
                    QrItem(
                        id = id,
                        textContent = unescapeJson(textContent),
                        acquireType = acquireType,
                        timestamp = Instant.fromEpochMilliseconds(timestampMs),
                        imageData = null
                    )
                )
            }
        } catch (e: Exception) {
            // Return empty on parse error
        }
        return items
    }

    private fun escapeJson(str: String): String {
        return str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun unescapeJson(str: String): String {
        return str
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

    companion object {
        private const val STORAGE_KEY = "qr_history"
    }
}

// Simple JSON parsing helpers
private fun parseJsonArray(json: String): List<String> {
    val trimmed = json.trim()
    if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return emptyList()

    val content = trimmed.substring(1, trimmed.length - 1)
    if (content.isBlank()) return emptyList()

    val items = mutableListOf<String>()
    var depth = 0
    var start = 0

    for (i in content.indices) {
        when (content[i]) {
            '{' -> depth++
            '}' -> {
                depth--
                if (depth == 0) {
                    items.add(content.substring(start, i + 1).trim())
                    start = i + 2
                }
            }
        }
    }
    return items
}

private fun extractJsonString(json: String, key: String): String? {
    val pattern = "\"$key\":\"([^\"]*)\""
    val regex = Regex(pattern)
    return regex.find(json)?.groupValues?.get(1)
}

private fun extractJsonInt(json: String, key: String): Int? {
    val pattern = "\"$key\":(\\d+)"
    val regex = Regex(pattern)
    return regex.find(json)?.groupValues?.get(1)?.toIntOrNull()
}

private fun extractJsonLong(json: String, key: String): Long? {
    val pattern = "\"$key\":(\\d+)"
    val regex = Regex(pattern)
    return regex.find(json)?.groupValues?.get(1)?.toLongOrNull()
}

// localStorage interop
@JsFun("(key) => localStorage.getItem(key)")
private external fun localStorageGetItem(key: String): String?

@JsFun("(key, value) => localStorage.setItem(key, value)")
private external fun localStorageSetItem(key: String, value: String)
