package rs.covtakt.notifications.persistence

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "notify_table")
class NotifyRecord constructor(
    @ColumnInfo(name = "title")
    var title:String?=null,
    @ColumnInfo(name = "content")
    var content:String?=null,
    var  dateInfected:Long?=0
) :Parcelable{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    @ColumnInfo(name = "timestamp")
    var timestamp: Long = System.currentTimeMillis()
}
