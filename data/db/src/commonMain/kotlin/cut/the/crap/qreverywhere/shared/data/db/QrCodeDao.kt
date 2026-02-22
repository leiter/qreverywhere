package cut.the.crap.qreverywhere.shared.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for QR code history operations - cross-platform compatible
 */
@Dao
interface QrCodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QrCodeDbEntity)

    @Update
    suspend fun update(entity: QrCodeDbEntity)

    @Delete
    suspend fun delete(entity: QrCodeDbEntity)

    @Query("SELECT * FROM qrcode_history ORDER BY timestamp DESC")
    fun getAllAsFlow(): Flow<List<QrCodeDbEntity>>

    @Query("SELECT * FROM qrcode_history WHERE id = :id")
    suspend fun getById(id: Int): QrCodeDbEntity?

    @Query("DELETE FROM qrcode_history")
    suspend fun deleteAll()
}
