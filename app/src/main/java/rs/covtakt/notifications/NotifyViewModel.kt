package rs.covtakt.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import rs.covtakt.notifications.persistence.NotifyRecord
import rs.covtakt.notifications.persistence.NotifyRecordRepository
import rs.covtakt.streetpass.persistence.StreetPassRecordDatabase

class NotifyViewModel (app: Application) : AndroidViewModel(app) {

    private var repo: NotifyRecordRepository

    var allRecords: LiveData<List<NotifyRecord>>

    init {
        val recordDao = StreetPassRecordDatabase.getDatabase(app).recordNotifyDao()
        repo = NotifyRecordRepository(recordDao)
        allRecords = repo.allRecords
    }


}
