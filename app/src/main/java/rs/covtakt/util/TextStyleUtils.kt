package rs.covtakt.util

import android.text.SpannableString
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import rs.covtakt.R

object TextStyleUtils {

    fun setTime(view: TextView, text: String?, spanText: String?) {

        val context = view.context
        val s2 = if(spanText.isNullOrEmpty()) "" else spanText
        val s1 = "$text\n"
        if (text.isNullOrEmpty()) return
        val s = "$s1 $s2"
        val ss = SpannableString(s)
        ss.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.black)), s1.length, s.length, SpannedString.SPAN_EXCLUSIVE_EXCLUSIVE)
        view.text = ss

    }

}