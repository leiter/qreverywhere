package cut.the.crap.qrrepository.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface QrCodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQrItem(qrItem: QrCodeDbItem)

    @Delete
    suspend fun deleteQrItem(qrItem: QrCodeDbItem)

    @Query("SELECT * FROM qrcode_history ORDER BY timestamp DESC")
    fun getCompleteHistory(): LiveData<List<QrCodeDbItem>>

    @Update
    suspend fun update(qrItem: QrCodeDbItem)

}