package cut.the.crap.qreverywhere

import android.content.Context
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import cut.the.crap.qreverywhere.shared.presentation.state.State
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import cut.the.crap.qrrepository.Acquire
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Type alias for backward compatibility
 * This allows existing code to continue using MainActivityViewModel
 * while actually using the shared KMP MainViewModel
 */
typealias MainActivityViewModel = MainViewModel

/**
 * Constants for backward compatibility
 */
const val IMAGE_DIRECTORY = "QrEveryWhere"

/**
 * Extension properties and functions for backward compatibility
 * These bridge the gap between the old Android-specific API and the new shared API
 */

// Property for accessing the detail view item (old API)
val MainViewModel.detailViewQrCodeItem: cut.the.crap.qrrepository.QrItem
    get() = detailViewItem.value?.toAndroidQrItem() ?: run {
        val bitmap = android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888)
        cut.the.crap.qrrepository.QrItem(
            id = 0,
            textContent = "",
            acquireType = cut.the.crap.qrrepository.Acquire.EMPTY_DEFAULT,
            img = bitmap,
            timestamp = 0L
        )
    }

// StateFlow for detail view state (old API)
val MainViewModel.detailViewQrCodeItemState: StateFlow<State<cut.the.crap.qrrepository.QrItem>?>
    get() = detailViewState.map { state ->
        when (state) {
            is State.Loading -> State.loading(state.data?.toAndroidQrItem(), state.showLoading, state.progress)
            is State.Success -> State.success(state.data.toAndroidQrItem())
            is State.Error -> State.error(state.message, state.data?.toAndroidQrItem(), state.throwable)
            null -> null
        }
    } as? StateFlow<State<cut.the.crap.qrrepository.QrItem>?> ?: MutableStateFlow(null)

// Method for saving QR item from file (old API)
fun MainViewModel.saveQrItemFromFile(textContent: String, @Acquire.Type type: Int) {
    val acquireType = when (type) {
        Acquire.FROM_FILE -> AcquireType.FROM_FILE
        Acquire.SCANNED -> AcquireType.SCANNED
        Acquire.CREATED -> AcquireType.CREATED
        else -> AcquireType.EMPTY_DEFAULT
    }
    saveQrItemFromText(textContent, acquireType)
}

// Method for saving QR image with context (old API - context no longer needed)
fun MainViewModel.saveQrImageOfDetailView(context: Context) {
    saveQrImageOfDetailView()
}

// Extension to convert shared historyData to Android QrItem list
fun MainViewModel.getAndroidHistoryData(): StateFlow<List<cut.the.crap.qrrepository.QrItem>> {
    return historyData.map { items ->
        items.map { it.toAndroidQrItem() }
    } as? StateFlow<List<cut.the.crap.qrrepository.QrItem>> ?: MutableStateFlow(emptyList())
}

// Extension to save Android QrItem
fun MainViewModel.saveQrItem(qrItem: cut.the.crap.qrrepository.QrItem) {
    saveQrItem(qrItem.toSharedQrItem())
}

// Extension to set detail view with Android QrItem
fun MainViewModel.setDetailViewItem(qrItem: cut.the.crap.qrrepository.QrItem) {
    setDetailViewItem(qrItem.toSharedQrItem())
}

/**
 * Convert Android QrItem to shared QrItem
 */
private fun cut.the.crap.qrrepository.QrItem.toSharedQrItem(): cut.the.crap.qreverywhere.shared.domain.model.QrItem {
    val imageData = img.toByteArray()

    val acquireType = when (this.acquireType) {
        Acquire.SCANNED -> AcquireType.SCANNED
        Acquire.CREATED -> AcquireType.CREATED
        Acquire.FROM_FILE -> AcquireType.FROM_FILE
        Acquire.ERROR_OCCURRED -> AcquireType.ERROR_OCCURRED
        else -> AcquireType.EMPTY_DEFAULT
    }

    return cut.the.crap.qreverywhere.shared.domain.model.QrItem(
        id = this.id,
        textContent = this.textContent,
        acquireType = acquireType,
        timestamp = kotlinx.datetime.Instant.fromEpochMilliseconds(this.timestamp),
        imageData = imageData
    )
}

/**
 * Convert Bitmap to ByteArray
 */
private fun android.graphics.Bitmap.toByteArray(): ByteArray {
    val stream = java.io.ByteArrayOutputStream()
    compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

/**
 * Convert shared QrItem to Android QrItem
 */
private fun cut.the.crap.qreverywhere.shared.domain.model.QrItem.toAndroidQrItem(): cut.the.crap.qrrepository.QrItem {
    val bitmap = imageData?.let { data ->
        android.graphics.BitmapFactory.decodeByteArray(data, 0, data.size)
    } ?: android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888)

    val acquireType = when (this.acquireType) {
        AcquireType.SCANNED -> Acquire.SCANNED
        AcquireType.CREATED -> Acquire.CREATED
        AcquireType.FROM_FILE -> Acquire.FROM_FILE
        AcquireType.ERROR_OCCURRED -> Acquire.ERROR_OCCURRED
        AcquireType.EMPTY_DEFAULT -> Acquire.EMPTY_DEFAULT
    }

    return cut.the.crap.qrrepository.QrItem(
        id = this.id,
        textContent = this.textContent,
        acquireType = acquireType,
        img = bitmap,
        timestamp = this.timestamp.toEpochMilliseconds()
    )
}