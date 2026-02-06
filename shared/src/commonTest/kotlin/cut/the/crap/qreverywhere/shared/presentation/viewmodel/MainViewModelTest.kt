package cut.the.crap.qreverywhere.shared.presentation.viewmodel

import cut.the.crap.qreverywhere.shared.data.FakeQrRepository
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import cut.the.crap.qreverywhere.shared.domain.model.QrItem
import cut.the.crap.qreverywhere.shared.domain.usecase.FakeQrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.FakeSaveImageToFileUseCase
import cut.the.crap.qreverywhere.shared.domain.usecase.FakeUserPreferences
import cut.the.crap.qreverywhere.shared.presentation.state.State
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for MainViewModel
 *
 * Note: These tests require Dispatchers.Main to be available, which is only supported on Android.
 * In KMP, ViewModel testing requires platform-specific test setup.
 * These tests are marked @Ignore for commonTest but can be run on Android with proper setup.
 *
 * TODO: Move these tests to androidUnitTest source set where Dispatchers.Main is available.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Ignore("ViewModel tests require Dispatchers.Main - run on Android or use platform-specific test runner")
class MainViewModelTest {
    private lateinit var viewModel: MainViewModel
    private lateinit var fakeRepository: FakeQrRepository
    private lateinit var fakeQrCodeGenerator: FakeQrCodeGenerator
    private lateinit var fakeSaveImageUseCase: FakeSaveImageToFileUseCase
    private lateinit var fakeUserPreferences: FakeUserPreferences

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeQrRepository()
        fakeQrCodeGenerator = FakeQrCodeGenerator()
        fakeSaveImageUseCase = FakeSaveImageToFileUseCase()
        fakeUserPreferences = FakeUserPreferences()
    }

    @AfterTest
    fun tearDown() {
        fakeRepository.clear()
        fakeQrCodeGenerator.reset()
        fakeSaveImageUseCase.reset()
        fakeUserPreferences.reset()
    }

    private fun createViewModel(): MainViewModel {
        return MainViewModel(
            qrRepository = fakeRepository,
            qrCodeGenerator = fakeQrCodeGenerator,
            saveImageUseCase = fakeSaveImageUseCase,
            userPreferences = fakeUserPreferences
        )
    }

    // Helper to wait for viewModelScope coroutines to complete
    private suspend fun waitForViewModelCoroutines() {
        delay(100)
    }

    // ==================== History Loading Tests ====================

    @Test
    fun `viewModel loads history on init`() = runTest {
        // Given some items in repository
        val item = createTestQrItem("test content")
        fakeRepository.insertQrItem(item)

        // When viewModel is created
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        // Then history contains the item
        val history = viewModel.historyData.value
        assertEquals(1, history.size)
        assertEquals("test content", history[0].textContent)
    }

    @Test
    fun `viewModel starts with empty history when repository is empty`() = runTest {
        // When viewModel is created with empty repository
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        // Then history is empty
        assertTrue(viewModel.historyData.value.isEmpty())
    }

    // ==================== Detail View Tests ====================

    @Test
    fun `setDetailViewItem updates detailViewItem state`() = runTest {
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        val item = createTestQrItem("detail item")

        // When
        viewModel.setDetailViewItem(item)

        // Then
        assertEquals(item, viewModel.detailViewItem.value)
    }

    // ==================== Save QR Item Tests ====================

    @Test
    fun `saveQrItem inserts item to repository`() = runTest {
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        val item = createTestQrItem("new item")

        // When
        viewModel.saveQrItem(item)
        waitForViewModelCoroutines()

        // Then
        val items = fakeRepository.getItemsSnapshot()
        assertEquals(1, items.size)
    }

    @Test
    fun `saveQrItem handles repository error gracefully`() = runTest {
        fakeRepository.shouldThrowOnInsert = true
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        val item = createTestQrItem("new item")

        // When - should not throw
        viewModel.saveQrItem(item)
        waitForViewModelCoroutines()

        // Then - item not saved due to error
        assertTrue(fakeRepository.getItemsSnapshot().isEmpty())
    }

    // ==================== Save QR Item From Text Tests ====================

    @Test
    fun `saveQrItemFromText generates and saves QR code`() = runTest {
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        // When
        viewModel.saveQrItemFromText("https://example.com", AcquireType.CREATED)
        waitForViewModelCoroutines()

        // Then
        assertEquals("https://example.com", fakeQrCodeGenerator.lastGeneratedText)
        val items = fakeRepository.getItemsSnapshot()
        assertEquals(1, items.size)
        assertEquals("https://example.com", items[0].textContent)
        assertEquals(AcquireType.CREATED, items[0].acquireType)
    }

    @Test
    fun `saveQrItemFromText uses user color preferences`() = runTest {
        fakeUserPreferences.setForegroundColor(0xFF0000FF.toInt()) // Blue
        fakeUserPreferences.setBackgroundColor(0xFFFF00FF.toInt()) // Magenta
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        // When
        viewModel.saveQrItemFromText("test", AcquireType.CREATED)
        waitForViewModelCoroutines()

        // Then
        assertEquals(0xFF0000FF.toInt(), fakeQrCodeGenerator.lastForegroundColor)
        assertEquals(0xFFFF00FF.toInt(), fakeQrCodeGenerator.lastBackgroundColor)
    }

    @Test
    fun `saveQrItemFromText sets loading state for FROM_FILE acquire type`() = runTest {
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        // When
        viewModel.saveQrItemFromText("scanned content", AcquireType.FROM_FILE)

        // Then - loading state should be set immediately
        val state = viewModel.detailViewState.value
        assertNotNull(state)
        // After completion it becomes success
        waitForViewModelCoroutines()
        val finalState = viewModel.detailViewState.value
        assertIs<State.Success<QrItem>>(finalState)
    }

    @Test
    fun `saveQrItemFromText sets error state on generator failure`() = runTest {
        fakeQrCodeGenerator.shouldThrow = true
        fakeQrCodeGenerator.generatorException = RuntimeException("QR generation failed")
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        // When
        viewModel.saveQrItemFromText("test", AcquireType.FROM_FILE)
        waitForViewModelCoroutines()

        // Then
        val state = viewModel.detailViewState.value
        assertIs<State.Error<QrItem>>(state)
        assertEquals("QR generation failed", state.message)
    }

    @Test
    fun `saveQrItemFromText updates detailViewItem on success`() = runTest {
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        // When
        viewModel.saveQrItemFromText("content", AcquireType.CREATED)
        waitForViewModelCoroutines()

        // Then
        val detailItem = viewModel.detailViewItem.value
        assertNotNull(detailItem)
        assertEquals("content", detailItem.textContent)
    }

    // ==================== Delete Tests ====================

    @Test
    fun `deleteQrItem removes item from repository`() = runTest {
        val item = createTestQrItem("to delete", id = 1)
        fakeRepository.insertQrItem(item)
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        // Verify item exists
        assertEquals(1, fakeRepository.getItemsSnapshot().size)

        // When
        viewModel.deleteQrItem(item)
        waitForViewModelCoroutines()

        // Then
        assertTrue(fakeRepository.getItemsSnapshot().isEmpty())
    }

    @Test
    fun `deleteCurrentDetailView deletes the current detail item`() = runTest {
        val item = createTestQrItem("detail to delete", id = 1)
        fakeRepository.insertQrItem(item)
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        viewModel.setDetailViewItem(item)

        // When
        viewModel.deleteCurrentDetailView()
        waitForViewModelCoroutines()

        // Then
        assertTrue(fakeRepository.getItemsSnapshot().isEmpty())
    }

    @Test
    fun `deleteCurrentDetailView does nothing when no detail item set`() = runTest {
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        // When - should not throw
        viewModel.deleteCurrentDetailView()
        waitForViewModelCoroutines()

        // Then - no error, nothing happens
        assertNull(viewModel.detailViewItem.value)
    }

    @Test
    fun `removeHistoryItem removes item at position`() = runTest {
        val item1 = createTestQrItem("item 1", id = 1)
        val item2 = createTestQrItem("item 2", id = 2)
        fakeRepository.insertQrItem(item1)
        fakeRepository.insertQrItem(item2)
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        // When - remove first item (index 0)
        // Note: history is sorted by timestamp DESC, so we need to account for that
        viewModel.removeHistoryItem(0)
        waitForViewModelCoroutines()

        // Then
        assertEquals(1, fakeRepository.getItemsSnapshot().size)
    }

    @Test
    fun `removeHistoryItem handles negative position`() = runTest {
        val item = createTestQrItem("item", id = 1)
        fakeRepository.insertQrItem(item)
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        // When - negative position should be ignored
        viewModel.removeHistoryItem(-1)
        waitForViewModelCoroutines()

        // Then - item still exists
        assertEquals(1, fakeRepository.getItemsSnapshot().size)
    }

    // ==================== Clear History Tests ====================

    @Test
    fun `clearHistory removes all items`() = runTest {
        fakeRepository.insertQrItem(createTestQrItem("item 1", id = 1))
        fakeRepository.insertQrItem(createTestQrItem("item 2", id = 2))
        fakeRepository.insertQrItem(createTestQrItem("item 3", id = 3))
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        // Verify items exist
        assertEquals(3, fakeRepository.getItemsSnapshot().size)

        // When
        viewModel.clearHistory()
        waitForViewModelCoroutines()

        // Then
        assertTrue(fakeRepository.getItemsSnapshot().isEmpty())
    }

    // ==================== Save QR Image Tests ====================

    @Test
    fun `saveQrImageOfDetailView emits success with file path`() = runTest {
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        val item = createTestQrItem("test", imageData = byteArrayOf(1, 2, 3))
        viewModel.setDetailViewItem(item)

        // Collect events
        val events = mutableListOf<State<String?>>()
        val job = launch {
            viewModel.saveQrImageEvent.collect { events.add(it) }
        }

        // When
        viewModel.saveQrImageOfDetailView()
        waitForViewModelCoroutines()

        // Then - should have loading and success events
        assertTrue(events.isNotEmpty())
        val successEvent = events.lastOrNull { it is State.Success }
        assertNotNull(successEvent)
        assertIs<State.Success<String?>>(successEvent)
        assertEquals("/fake/path/qr_code.png", successEvent.data)

        job.cancel()
    }

    @Test
    fun `saveQrImageOfDetailView emits error when no image data`() = runTest {
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        val item = createTestQrItem("test", imageData = null)
        viewModel.setDetailViewItem(item)

        // Collect events
        val events = mutableListOf<State<String?>>()
        val job = launch {
            viewModel.saveQrImageEvent.collect { events.add(it) }
        }

        // When
        viewModel.saveQrImageOfDetailView()
        waitForViewModelCoroutines()

        // Then
        val errorEvent = events.lastOrNull { it is State.Error }
        assertNotNull(errorEvent)
        assertIs<State.Error<String?>>(errorEvent)
        assertEquals("No image data available", errorEvent.message)

        job.cancel()
    }

    @Test
    fun `saveQrImageOfDetailView does nothing when no detail item`() = runTest {
        viewModel = createViewModel()
        waitForViewModelCoroutines()

        // Collect events
        val events = mutableListOf<State<String?>>()
        val job = launch {
            viewModel.saveQrImageEvent.collect { events.add(it) }
        }

        // When - no detail item set
        viewModel.saveQrImageOfDetailView()
        waitForViewModelCoroutines()

        // Then - no events emitted
        assertTrue(events.isEmpty())

        job.cancel()
    }

    // ==================== Helper Methods ====================

    private fun createTestQrItem(
        textContent: String,
        id: Int = 0,
        acquireType: AcquireType = AcquireType.SCANNED,
        imageData: ByteArray? = null
    ): QrItem {
        return QrItem(
            id = id,
            textContent = textContent,
            acquireType = acquireType,
            timestamp = Clock.System.now(),
            imageData = imageData
        )
    }
}
