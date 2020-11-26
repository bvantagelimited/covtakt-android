package rs.covtakt.streetpass.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import rs.covtakt.notifications.persistence.NotifyRecord
import rs.covtakt.notifications.persistence.NotifyRecordDao
import rs.covtakt.status.persistence.StatusRecord
import rs.covtakt.status.persistence.StatusRecordDao


@Database(
    entities = arrayOf(StreetPassRecord::class, StatusRecord::class,NotifyRecord::class),
    version = 2,
    exportSchema = true
)
abstract class StreetPassRecordDatabase : RoomDatabase() {

    abstract fun recordDao(): StreetPassRecordDao
    abstract fun statusDao(): StatusRecordDao
    abstract fun recordNotifyDao(): NotifyRecordDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: StreetPassRecordDatabase? = null

        fun getDatabase(context: Context): StreetPassRecordDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    StreetPassRecordDatabase::class.java,
                    "record_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
