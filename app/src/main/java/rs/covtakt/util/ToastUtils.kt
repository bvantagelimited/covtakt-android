package rs.covtakt.util

import android.content.Context
import android.widget.Toast

object ToastUtils {

    fun showShort(context: Context,msg:String?){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT)
    }
    fun showLong(context: Context,msg:String?){
        Toast.makeText(context,msg,Toast.LENGTH_LONG)
    }
}