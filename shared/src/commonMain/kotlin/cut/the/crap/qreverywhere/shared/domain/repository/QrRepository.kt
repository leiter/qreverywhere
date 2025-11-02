package cut.the.crap.qreverywhere.shared.domain.repository

import cut.the.crap.qreverywhere.shared.domain.model.QrItem
import kotlinx.coroutines.flow.Flow

/**
 * Common repository interface for QR code operations
 * Platform-specific implementations will handle actual storage
 */
interface QrRepository {

    /**
     * Get all QR code items as a Flow
     */
    fun getQrHistory(): Flow<List<QrItem>>

    /**
     * Insert a new QR code item
     */
    suspend fun insertQrItem(qrItem: QrItem)

    /**
     * Delete a QR code item
     */
    suspend fun deleteQrItem(qrItem: QrItem)

    /**
     * Delete all QR code items
     */
    suspend fun deleteAll()

    /**
     * Get a specific QR code item by ID
     */
    suspend fun getQrItem(id: Int): QrItem?

    /**
     * Update an existing QR code item
     */
    suspend fun updateQrItem(qrItem: QrItem)
}