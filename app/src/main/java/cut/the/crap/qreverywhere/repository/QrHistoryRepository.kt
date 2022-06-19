package cut.the.crap.qreverywhere.repository

import cut.the.crap.qreverywhere.db.QrCodeDao
import cut.the.crap.qreverywhere.db.QrCodeItem
import javax.inject.Inject



class QrHistoryRepository @Inject constructor(
    private val qrCodeDao: QrCodeDao
) {

    suspend fun insertQrItem(qrCodeItem: QrCodeItem) = qrCodeDao.insertQrItem(qrCodeItem)

    suspend fun deleteQrItem(qrCodeItem: QrCodeItem) = qrCodeDao.deleteQrItem(qrCodeItem)

    fun getCompleteQrCodeHistory() = qrCodeDao.getCompleteHistory()

    fun getAllRunsSortedByTimeInMillis() = qrCodeDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByDistance() = qrCodeDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByCaloriesBurned() = qrCodeDao.getAllRunsSortedByCaloriesBurned()

    fun getAllRunsSortedByAvgSpeed() = qrCodeDao.getAllRunsSortedByAvgSpeed()

    fun getTotalDistance() = qrCodeDao.getTotalDistance()

    fun getTotalTimeInMillis() = qrCodeDao.getTotalTimeInMillis()

    fun getTotalAvgSpeed() = qrCodeDao.getTotalAvgSpeed()

    fun getTotalCaloriesBurned() = qrCodeDao.getTotalCaloriesBurned()
}