package cut.the.crap.qreverywhere.shared.data

import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import cut.the.crap.qreverywhere.shared.domain.model.QrItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for QrRepository behavior using FakeQrRepository
 * These tests verify the repository contract that all implementations must follow
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RoomQrRepositoryTest {
    private val repository = FakeQrRepository()

    private fun createTestQrItem(
        textContent: String,
        id: Int = 0,
        acquireType: AcquireType = AcquireType.SCANNED
    ): QrItem {
        return QrItem(
            id = id,
            textContent = textContent,
            acquireType = acquireType,
            timestamp = Clock.System.now()
        )
    }

    // ==================== Insert Tests ====================

    @Test
    fun `insertQrItem adds item to repository`() = runTest {
        val item = createTestQrItem("test content")

        repository.insertQrItem(item)

        val history = repository.getQrHistory().first()
        assertEquals(1, history.size)
        assertEquals("test content", history[0].textContent)
    }

    @Test
    fun `insertQrItem auto-generates id for new items`() = runTest {
        val item = createTestQrItem("test", id = 0)

        repository.insertQrItem(item)

        val history = repository.getQrHistory().first()
        assertTrue(history[0].id > 0)
    }

    @Test
    fun `insertQrItem preserves non-zero id`() = runTest {
        val item = createTestQrItem("test", id = 42)

        repository.insertQrItem(item)

        val history = repository.getQrHistory().first()
        assertEquals(42, history[0].id)
    }

    @Test
    fun `multiple insertQrItem calls add multiple items`() = runTest {
        repository.insertQrItem(createTestQrItem("first"))
        repository.insertQrItem(createTestQrItem("second"))
        repository.insertQrItem(createTestQrItem("third"))

        val history = repository.getQrHistory().first()
        assertEquals(3, history.size)
    }

    // ==================== Delete Tests ====================

    @Test
    fun `deleteQrItem removes item from repository`() = runTest {
        val item = createTestQrItem("to delete", id = 1)
        repository.insertQrItem(item)

        repository.deleteQrItem(item)

        val history = repository.getQrHistory().first()
        assertTrue(history.isEmpty())
    }

    @Test
    fun `deleteQrItem only removes matching item`() = runTest {
        val item1 = createTestQrItem("keep", id = 1)
        val item2 = createTestQrItem("delete", id = 2)
        repository.insertQrItem(item1)
        repository.insertQrItem(item2)

        repository.deleteQrItem(item2)

        val history = repository.getQrHistory().first()
        assertEquals(1, history.size)
        assertEquals("keep", history[0].textContent)
    }

    @Test
    fun `deleteQrItem with non-existent id does nothing`() = runTest {
        val existingItem = createTestQrItem("exists", id = 1)
        repository.insertQrItem(existingItem)

        val nonExistentItem = createTestQrItem("ghost", id = 999)
        repository.deleteQrItem(nonExistentItem)

        val history = repository.getQrHistory().first()
        assertEquals(1, history.size)
    }

    // ==================== DeleteAll Tests ====================

    @Test
    fun `deleteAll removes all items`() = runTest {
        repository.insertQrItem(createTestQrItem("one", id = 1))
        repository.insertQrItem(createTestQrItem("two", id = 2))
        repository.insertQrItem(createTestQrItem("three", id = 3))

        repository.deleteAll()

        val history = repository.getQrHistory().first()
        assertTrue(history.isEmpty())
    }

    @Test
    fun `deleteAll on empty repository is no-op`() = runTest {
        repository.deleteAll()

        val history = repository.getQrHistory().first()
        assertTrue(history.isEmpty())
    }

    // ==================== GetById Tests ====================

    @Test
    fun `getQrItem returns item by id`() = runTest {
        val item = createTestQrItem("find me", id = 5)
        repository.insertQrItem(item)

        val found = repository.getQrItem(5)

        assertNotNull(found)
        assertEquals("find me", found.textContent)
    }

    @Test
    fun `getQrItem returns null for non-existent id`() = runTest {
        repository.insertQrItem(createTestQrItem("exists", id = 1))

        val found = repository.getQrItem(999)

        assertNull(found)
    }

    // ==================== Update Tests ====================

    @Test
    fun `updateQrItem modifies existing item`() = runTest {
        val original = createTestQrItem("original", id = 1)
        repository.insertQrItem(original)

        val updated = original.copy(textContent = "updated")
        repository.updateQrItem(updated)

        val found = repository.getQrItem(1)
        assertNotNull(found)
        assertEquals("updated", found.textContent)
    }

    @Test
    fun `updateQrItem with non-existent id does nothing`() = runTest {
        val item = createTestQrItem("existing", id = 1)
        repository.insertQrItem(item)

        val nonExistent = createTestQrItem("ghost", id = 999)
        repository.updateQrItem(nonExistent)

        val history = repository.getQrHistory().first()
        assertEquals(1, history.size)
        assertEquals("existing", history[0].textContent)
    }

    // ==================== Flow Tests ====================

    @Test
    fun `getQrHistory returns items sorted by timestamp descending`() = runTest {
        // Note: FakeQrRepository sorts by timestamp DESC
        val item1 = createTestQrItem("first")
        repository.insertQrItem(item1)

        // Slight delay to ensure different timestamps
        val item2 = createTestQrItem("second")
        repository.insertQrItem(item2)

        val item3 = createTestQrItem("third")
        repository.insertQrItem(item3)

        val history = repository.getQrHistory().first()
        assertEquals(3, history.size)
        // Most recent should be first (sorted DESC)
        assertEquals("third", history[0].textContent)
    }

    @Test
    fun `getQrHistory flow emits updates when items change`() = runTest {
        // Initial state
        val initialHistory = repository.getQrHistory().first()
        assertTrue(initialHistory.isEmpty())

        // Add item
        repository.insertQrItem(createTestQrItem("new item", id = 1))

        // Flow should emit updated list
        val updatedHistory = repository.getQrHistory().first()
        assertEquals(1, updatedHistory.size)
    }

    // ==================== AcquireType Tests ====================

    @Test
    fun `repository preserves AcquireType SCANNED`() = runTest {
        val item = createTestQrItem("scanned", acquireType = AcquireType.SCANNED)
        repository.insertQrItem(item)

        val history = repository.getQrHistory().first()
        assertEquals(AcquireType.SCANNED, history[0].acquireType)
    }

    @Test
    fun `repository preserves AcquireType CREATED`() = runTest {
        val item = createTestQrItem("created", acquireType = AcquireType.CREATED)
        repository.insertQrItem(item)

        val history = repository.getQrHistory().first()
        assertEquals(AcquireType.CREATED, history[0].acquireType)
    }

    @Test
    fun `repository preserves AcquireType FROM_FILE`() = runTest {
        val item = createTestQrItem("from file", acquireType = AcquireType.FROM_FILE)
        repository.insertQrItem(item)

        val history = repository.getQrHistory().first()
        assertEquals(AcquireType.FROM_FILE, history[0].acquireType)
    }

    // ==================== ByteArray/Image Data Tests ====================

    @Test
    fun `repository preserves image data`() = runTest {
        val imageData = byteArrayOf(1, 2, 3, 4, 5)
        val item = QrItem(
            id = 0,
            textContent = "with image",
            acquireType = AcquireType.CREATED,
            timestamp = Clock.System.now(),
            imageData = imageData
        )
        repository.insertQrItem(item)

        val history = repository.getQrHistory().first()
        assertNotNull(history[0].imageData)
        assertTrue(imageData.contentEquals(history[0].imageData!!))
    }

    @Test
    fun `repository handles null image data`() = runTest {
        val item = QrItem(
            id = 0,
            textContent = "no image",
            acquireType = AcquireType.SCANNED,
            timestamp = Clock.System.now(),
            imageData = null
        )
        repository.insertQrItem(item)

        val history = repository.getQrHistory().first()
        assertNull(history[0].imageData)
    }
}
