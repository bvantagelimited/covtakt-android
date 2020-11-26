package rs.covtakt.util

import rs.covtakt.util.timeago.TimeAgo
import rs.covtakt.util.timeago.TimeAgoMessages
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {

    fun convertTime(time: Long): String? {
        val date = Date(time)
        val format: Format = SimpleDateFormat("MM/dd/yyyy HH:mm")
        return format.format(date)
    }

    fun formatTimeAgo(j: Long,messages: TimeAgoMessages): String? {
        return TimeAgo.using(j,messages)
    }
}