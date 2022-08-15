package cut.the.crap.qreverywhere.repository

import androidx.lifecycle.LiveData
import cut.the.crap.qreverywhere.db.QrCodeDao
import cut.the.crap.qreverywhere.db.QrCodeItem
import javax.inject.Inject



class QrHistoryRepository @Inject constructor(
    private val qrCodeDao: QrCodeDao
) {

    suspend fun insertQrItem(qrCodeItem: QrCodeItem) = qrCodeDao.insertQrItem(qrCodeItem)

    suspend fun deleteQrItem(qrCodeItem: QrCodeItem) = qrCodeDao.deleteQrItem(qrCodeItem)

    suspend fun updateQrItem(qrCodeItem: QrCodeItem) = qrCodeDao.update(qrCodeItem)

    fun getCompleteQrCodeHistory(): LiveData<List<QrCodeItem>> = qrCodeDao.getCompleteHistory()

}