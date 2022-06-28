package cut.the.crap.qreverywhere.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface QrCodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQrItem(qrItem: QrCodeItem)

    @Delete
    suspend fun deleteQrItem(qrItem: QrCodeItem)

    @Query("SELECT * FROM qrcode_history ORDER BY timestamp DESC")
    fun getCompleteHistory(): LiveData<List<QrCodeItem>>

    @Update
    suspend fun update(qrItem: QrCodeItem)

}