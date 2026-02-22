package cut.the.crap.qreverywhere.shared.data

import cut.the.crap.qreverywhere.shared.data.db.QrCodeDao
import cut.the.crap.qreverywhere.shared.data.db.toDbEntity
import cut.the.crap.qreverywhere.shared.data.db.toQrItem
import cut.the.crap.qreverywhere.shared.domain.model.QrItem
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-based QrRepository implementation for all platforms
 * Uses the cross-platform Room database
 */
class RoomQrRepository(
    private val dao: QrCodeDao
) : QrRepository {

    override fun getQrHistory(): Flow<List<QrItem>> {
        return dao.getAllAsFlow().map { entities ->
            entities.map { it.toQrItem() }
        }
    }

    override suspend fun insertQrItem(qrItem: QrItem) {
        dao.insert(qrItem.toDbEntity())
    }

    override suspend fun deleteQrItem(qrItem: QrItem) {
        dao.delete(qrItem.toDbEntity())
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override suspend fun getQrItem(id: Int): QrItem? {
        return dao.getById(id)?.toQrItem()
    }

    override suspend fun updateQrItem(qrItem: QrItem) {
        dao.update(qrItem.toDbEntity())
    }
}
