package rs.covtakt.onboarding

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.main_activity_onboarding.*
import rs.covtakt.LocaleManager
import rs.covtakt.Preference
import rs.covtakt.R

class PreOnboardingActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleManager.setNewLocale(this, applicationContext, Preference.getAppLanguage())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_onboarding)
        btn_onboardingStart.setOnClickListener {
            var intent = Intent(this, HowItWorksActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleManager.setNewLocale(this, applicationContext, Preference.getAppLanguage())
    }
}
