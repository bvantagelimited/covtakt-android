package rs.covtakt.notifications.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface NotifyRecordDao {

    @Query("SELECT * from notify_table ORDER BY timestamp ASC")
    fun getRecords(): LiveData<List<NotifyRecord>>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: NotifyRecord)
    @Query("SELECT * from notify_table ORDER BY timestamp ASC")
    fun getCurrentRecords(): List<NotifyRecord>
}