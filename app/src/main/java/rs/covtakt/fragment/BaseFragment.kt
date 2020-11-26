package rs.covtakt.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import rs.covtakt.LocaleManager
import rs.covtakt.Preference

open class BaseFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleManager.setNewLocale(activity!!, context!!, Preference.getAppLanguage())
        super.onCreate(savedInstanceState)
    }
}