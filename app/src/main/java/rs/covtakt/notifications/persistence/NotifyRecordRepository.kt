package rs.covtakt.notifications.persistence

import androidx.lifecycle.LiveData

class NotifyRecordRepository(private val recordDao: NotifyRecordDao) {
    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allRecords: LiveData<List<NotifyRecord>> = recordDao.getRecords()

    suspend fun insert(noti: NotifyRecord) {
        recordDao.insert(noti)
    }
}
