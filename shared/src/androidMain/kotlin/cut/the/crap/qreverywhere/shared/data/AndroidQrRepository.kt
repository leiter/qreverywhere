package cut.the.crap.qreverywhere.shared.data

import android.graphics.Bitmap
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import cut.the.crap.qreverywhere.shared.domain.model.QrItem
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qrrepository.QrHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import java.io.ByteArrayOutputStream

/**
 * Android implementation of QrRepository using existing Room database
 */
class AndroidQrRepository(
    private val roomRepository: QrHistoryRepository
) : QrRepository {

    override fun getQrHistory(): Flow<List<QrItem>> {
        return roomRepository.getCompleteQrCodeHistory().map { items ->
            items.map { it.toKmpQrItem() }
        }
    }

    override suspend fun insertQrItem(qrItem: QrItem) {
        val androidItem = qrItem.toAndroidQrItem()
        roomRepository.insertQrItem(androidItem)
    }

    override suspend fun deleteQrItem(qrItem: QrItem) {
        val androidItem = qrItem.toAndroidQrItem()
        roomRepository.deleteQrItem(androidItem)
    }

    override suspend fun deleteAll() {
        // Not available in current repository, implement with manual deletion
        val allItems = roomRepository.getCompleteQrCodeHistory().first()
        allItems.forEach { item ->
            roomRepository.deleteQrItem(item)
        }
    }

    override suspend fun getQrItem(id: Int): QrItem? {
        // Not available in current repository, implement with flow
        val allItems = roomRepository.getCompleteQrCodeHistory().first()
        return allItems.find { it.id == id }?.toKmpQrItem()
    }

    override suspend fun updateQrItem(qrItem: QrItem) {
        val androidItem = qrItem.toAndroidQrItem()
        roomRepository.updateQrItem(androidItem)
    }
}

// Extension functions for conversion
private fun cut.the.crap.qrrepository.QrItem.toKmpQrItem(): QrItem {
    return QrItem(
        id = this.id,
        textContent = this.textContent,
        acquireType = when (this.acquireType) {
            0 -> AcquireType.SCANNED
            1 -> AcquireType.CREATED
            2 -> AcquireType.FROM_FILE
            3 -> AcquireType.ERROR_OCCURRED
            else -> AcquireType.EMPTY_DEFAULT
        },
        timestamp = Instant.fromEpochMilliseconds(this.timestamp),
        imageData = this.img.toByteArray()
    )
}

private fun QrItem.toAndroidQrItem(): cut.the.crap.qrrepository.QrItem {
    // Convert image data back to Bitmap
    val bitmap = imageData?.let { data ->
        android.graphics.BitmapFactory.decodeByteArray(data, 0, data.size)
    } ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    return cut.the.crap.qrrepository.QrItem(
        id = this.id,
        textContent = this.textContent,
        acquireType = when (this.acquireType) {
            AcquireType.SCANNED -> 0
            AcquireType.CREATED -> 1
            AcquireType.FROM_FILE -> 2
            AcquireType.ERROR_OCCURRED -> 3
            AcquireType.EMPTY_DEFAULT -> 4
        },
        img = bitmap,
        timestamp = this.timestamp.toEpochMilliseconds()
    )
}

private fun Bitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}