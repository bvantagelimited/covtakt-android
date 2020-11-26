package rs.covtakt

import android.content.Context
import android.content.SharedPreferences

object Preference {
    private const val PREF_ID = "Tracer_pref"
    private const val IS_ONBOARDED = "IS_ONBOARDED"
    private const val COUNTRY_CODE = "COUNTRY_CODE"
    private const val PHONE_NUMBER = "PHONE_NUMBER"
    private const val CHECK_POINT = "CHECK_POINT"
    private const val HANDSHAKE_PIN = "HANDSHAKE_PIN"

    private const val NEXT_FETCH_TIME = "NEXT_FETCH_TIME"
    private const val EXPIRY_TIME = "EXPIRY_TIME"
    private const val LAST_FETCH_TIME = "LAST_FETCH_TIME"

    private const val LAST_PURGE_TIME = "LAST_PURGE_TIME"

    private const val ANNOUNCEMENT_NOTI = "ANNOUNCEMENT_NOTI"
    private const val APP_LANGUAGE = "LANGUAGE"

    fun putHandShakePin(context: Context, value: String) {
        context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .edit().putString(Preference.HANDSHAKE_PIN, value).apply()
    }

    fun getHandShakePin(context: Context): String {
        return context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .getString(Preference.HANDSHAKE_PIN, "AERTVC") ?: "AERTVC"
    }

    fun putIsOnBoarded(context: Context, value: Boolean) {
        context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .edit().putBoolean(Preference.IS_ONBOARDED, value).apply()
    }

    fun isOnBoarded(context: Context): Boolean {
        return context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .getBoolean(Preference.IS_ONBOARDED, false)
    }

    fun putPhoneNumber(context: Context, value: String) {
        context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .edit().putString(Preference.PHONE_NUMBER, value).apply()
    }

    fun getPhoneNumber(context: Context): String {
        return context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .getString(Preference.PHONE_NUMBER, "") ?: ""
    }

    fun putCountryCode(context: Context, value: String) {
        context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
                .edit().putString(Preference.COUNTRY_CODE, value).apply()
    }

    fun getCountryCode(context: Context): String {
        return context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
                .getString(Preference.COUNTRY_CODE, "") ?: "SR"
    }

    fun putCheckpoint(context: Context, value: Int) {
        context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .edit().putInt(Preference.CHECK_POINT, value).apply()
    }

    fun getCheckpoint(context: Context): Int {
        return context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .getInt(Preference.CHECK_POINT, 0)
    }

    fun getLastFetchTimeInMillis(context: Context): Long {
        return context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .getLong(
                    Preference.LAST_FETCH_TIME, 0
            )
    }

    fun putLastFetchTimeInMillis(context: Context, time: Long) {
        context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .edit().putLong(Preference.LAST_FETCH_TIME, time).apply()
    }

    fun putNextFetchTimeInMillis(context: Context, time: Long) {
        context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .edit().putLong(Preference.NEXT_FETCH_TIME, time).apply()
    }

    fun getNextFetchTimeInMillis(context: Context): Long {
        return context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .getLong(
                    Preference.NEXT_FETCH_TIME, 0
            )
    }

    fun putExpiryTimeInMillis(context: Context, time: Long) {
        context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .edit().putLong(Preference.EXPIRY_TIME, time).apply()
    }

    fun getExpiryTimeInMillis(context: Context): Long {
        return context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .getLong(
                    Preference.EXPIRY_TIME, 0
            )
    }

    fun putAnnouncement(context: Context, announcement: Boolean) {
        context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .edit().putBoolean(Preference.ANNOUNCEMENT_NOTI, announcement).apply()
    }

    fun getAnnouncement(context: Context): Boolean {
        return context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .getBoolean(Preference.ANNOUNCEMENT_NOTI, false)
    }

    fun putLastPurgeTime(context: Context, lastPurgeTime: Long) {
        context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .edit().putLong(Preference.LAST_PURGE_TIME, lastPurgeTime).apply()
    }

    fun getLastPurgeTime(context: Context): Long {
        return context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .getLong(Preference.LAST_PURGE_TIME, 0)
    }

    fun getAppLanguage(): String? {
//        if(BuildConfig.DEBUG){
//            return if(Locale.getDefault().language == "sr"){"sr"}else {"en"}
//        }else{
//            return "sr"
//        }
        return "sr"

    }

    fun putAppLanguage(context: Context, language: String) {
        context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
                .edit().putString(Preference.APP_LANGUAGE, language).apply()
    }

    fun registerListener(
        context: Context,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(
        context: Context,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        context.getSharedPreferences(Preference.PREF_ID, Context.MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(listener)
    }
}
