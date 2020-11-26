package rs.covtakt.notifications.persistence

import android.content.Context
import rs.covtakt.streetpass.persistence.StreetPassRecordDatabase

class NotifyRecordStorage(val context: Context) {

    val recordDao = StreetPassRecordDatabase.getDatabase(context).recordNotifyDao()

    suspend fun saveRecord(record: NotifyRecord) {
        recordDao.insert(record)
    }



    fun getAllRecords(): List<NotifyRecord> {
        return recordDao.getCurrentRecords()
    }


}
