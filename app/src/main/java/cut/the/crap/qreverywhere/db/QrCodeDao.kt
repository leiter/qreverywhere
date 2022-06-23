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








//    @Query("SELECT * FROM qrcode_history ORDER BY timeInMillis DESC")
//    fun getAllRunsSortedByTimeInMillis(): LiveData<List<QrCodeItem>>
//
//    @Query("SELECT * FROM qrcode_history ORDER BY caloriesBurned DESC")
//    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<QrCodeItem>>
//
//    @Query("SELECT * FROM qrcode_history ORDER BY distanceInMeters DESC")
//    fun getAllRunsSortedByDistance(): LiveData<List<QrCodeItem>>
//
//    @Query("SELECT * FROM qrcode_history ORDER BY avgSpeedInKMH DESC")
//    fun getAllRunsSortedByAvgSpeed(): LiveData<List<QrCodeItem>>
//
//    @Query("SELECT SUM(timeInMillis) FROM qrcode_history")
//    fun getTotalTimeInMillis(): LiveData<Long>
//
//    @Query("SELECT SUM(distanceInMeters) FROM qrcode_history")
//    fun getTotalDistance(): LiveData<Int>
//
//    @Query("SELECT AVG(avgSpeedInKMH) FROM qrcode_history")
//    fun getTotalAvgSpeed(): LiveData<Float>
//
//    @Query("SELECT SUM(caloriesBurned) FROM qrcode_history")
//    fun getTotalCaloriesBurned(): LiveData<Long>

}