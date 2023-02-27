package cut.the.crap.qrrepository.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QrCodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQrItem(qrItem: QrCodeDbItem)

    @Delete
    suspend fun deleteQrItem(qrItem: QrCodeDbItem)

    @Query("SELECT * FROM qrcode_history ORDER BY timestamp DESC")
    fun getCompleteHistory(): Flow<List<QrCodeDbItem>>

    @Update
    suspend fun update(qrItem: QrCodeDbItem)

}