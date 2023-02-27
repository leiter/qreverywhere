package cut.the.crap.qrrepository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.room.Room
import cut.the.crap.qrrepository.db.QrCodeDbItem
import cut.the.crap.qrrepository.db.toItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATABASE_NAME = "my_data"

class QrHistoryRepository(
    application: Context,
) {
    private val database = Room.databaseBuilder(
        application, cut.the.crap.qrrepository.db.QrDatabase::class.java, DATABASE_NAME)
        .fallbackToDestructiveMigration()
        .build()

    private val qrCodeDao: cut.the.crap.qrrepository.db.QrCodeDao = database.getDao()

    suspend fun insertQrItem(qrCodeItem: QrItem) = qrCodeDao.insertQrItem(qrCodeItem.toQrCodeDbItem())

    suspend fun deleteQrItem(qrCodeItem: QrItem) = qrCodeDao.deleteQrItem(qrCodeItem.toQrCodeDbItem())

    suspend fun updateQrItem(qrCodeItem: QrItem) = qrCodeDao.update(qrCodeItem.toQrCodeDbItem())

    fun getCompleteQrCodeHistory(): Flow<List<QrItem>> {
        return qrCodeDao.getCompleteHistory().map { transform(it) }
    }

    private fun transform(list: List<QrCodeDbItem>): List<QrItem> {
        return list.map { it.toItem() }
    }
}
