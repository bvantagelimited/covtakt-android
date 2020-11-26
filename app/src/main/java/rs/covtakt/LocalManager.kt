package rs.covtakt

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import java.util.*


object LocaleManager {

    @JvmStatic
    fun setNewLocale(c: Context, c2: Context, language: String?): Context {
        return updateResources(c, c2, language)
    }

    @SuppressLint("ApplySharedPref")
    private fun persistLanguage(language: String) {
        // use commit() instead of apply(), because sometimes we kill the application process immediately
        // which will prevent apply() to finish
    }

    private fun updateResources(context: Context, appContext: Context?, language: String?): Context {
        if(language == null){
            return context
        }
        val locale: Locale = if (language.equals("en", ignoreCase = true)) {
            Locale.ENGLISH
        } else if (language.equals("sr", ignoreCase = true)) {
            LocaleManager.serbianLatinLocale()!!
        } else {
            Locale.ENGLISH
        }


        return changeLanguage(locale, context, appContext)

    }
    fun getDefaultLocal():Locale{
        val language= Preference.getAppLanguage()
       return  if (language.equals("en", ignoreCase = true)) {
            Locale.ENGLISH
        } else if (language.equals("sr", ignoreCase = true)) {
            LocaleManager.serbianLatinLocale()!!
        } else {
            Locale.ENGLISH
        }
    }

    private fun serbianLatinLocale(): Locale? {
        var locale: Locale? = null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            for (checkLocale in Locale.getAvailableLocales()) {
                if (checkLocale.isO3Language.equals("srp") && checkLocale.country.equals("LATN") && checkLocale.variant.equals("")) {
                    locale = checkLocale
                }
            }
        } else {
            locale = Locale.Builder().setLanguage("sr").setRegion("RS").setScript("Latn").build()
        }
        return locale
    }

    private fun changeLanguage(locale: Locale, context: Context, appContext: Context?): Context {

        var resource = appContext?.resources
        var config = resource?.configuration
        config?.setLocale(locale)
        resource?.updateConfiguration(config, appContext?.resources?.displayMetrics)

        Log.e("language", "[INF] changeLanguage with locale = $locale")
        resource = context.resources
        config = resource.configuration
        config.setLocale(locale)
        resource.updateConfiguration(config, context.resources.displayMetrics)

        Locale.setDefault(locale)
        return context
    }

    fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
        config.locale = locale
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun setSystemLocale(config: Configuration, locale: Locale) {
        config.setLocale(locale)
    }

}