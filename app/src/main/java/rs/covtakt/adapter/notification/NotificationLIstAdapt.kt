package rs.covtakt.adapter.notification

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_local_notification.view.*
import rs.covtakt.LocaleManager.getDefaultLocal
import rs.covtakt.R
import rs.covtakt.notifications.persistence.NotifyRecord
import rs.covtakt.util.DateTimeUtils
import rs.covtakt.util.timeago.TimeAgoMessages


class NotificationLIstAdapt internal constructor(context: Context) :
        RecyclerView.Adapter<NotificationLIstAdapt.RecordViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var records = emptyList<NotifyRecord>() // Cached copy of records

    var messages: TimeAgoMessages?=null
    init {

         val locale = getDefaultLocal()
        messages = TimeAgoMessages.Builder().withLocale(locale).build()
    }



    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvContent: TextView = itemView.tvContent
        val tvDate :TextView = itemView.tvDate

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationLIstAdapt.RecordViewHolder {
        val itemView = inflater.inflate(R.layout.item_local_notification, parent, false)
        return RecordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NotificationLIstAdapt.RecordViewHolder, position: Int) {
        val current = records[position]
        holder.tvContent.text = current.content
        holder.tvDate.text = DateTimeUtils.formatTimeAgo(current?.dateInfected!!,messages!!)




    }

    override fun getItemCount(): Int {
        return records.size
    }

    fun setData(data:List<NotifyRecord>){
        records = data
        notifyDataSetChanged()
    }
}