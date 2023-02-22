package cut.the.crap.qrrepository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room

private const val DATABASE_NAME = "my_data"

class QrHistoryRepository(
    application: Context,
) {
    private val database = Room.databaseBuilder(application, cut.the.crap.qrrepository.db.QrDatabase::class.java, DATABASE_NAME)
        .fallbackToDestructiveMigration()
        .build()

    private val qrCodeDao: cut.the.crap.qrrepository.db.QrCodeDao = database.getDao()

    suspend fun insertQrItem(qrCodeItem: cut.the.crap.qrrepository.db.QrCodeItem) = qrCodeDao.insertQrItem(qrCodeItem)

    suspend fun deleteQrItem(qrCodeItem: cut.the.crap.qrrepository.db.QrCodeItem) = qrCodeDao.deleteQrItem(qrCodeItem)

    suspend fun updateQrItem(qrCodeItem: cut.the.crap.qrrepository.db.QrCodeItem) = qrCodeDao.update(qrCodeItem)

    fun getCompleteQrCodeHistory(): LiveData<List<cut.the.crap.qrrepository.db.QrCodeItem>> = qrCodeDao.getCompleteHistory()

}
