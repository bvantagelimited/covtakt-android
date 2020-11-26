package rs.covtakt

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import rs.covtakt.notifications.persistence.NotifyRecord
import rs.covtakt.notifications.persistence.NotifyRecordStorage
import rs.covtakt.onboarding.PreOnboardingActivity
import rs.covtakt.util.NotificationType
import kotlin.coroutines.CoroutineContext

class SplashActivity : AppCompatActivity() ,CoroutineScope{

    private val SPLASH_TIME: Long = 2000
    var needToUpdateApp = false

    private lateinit var mHandler: Handler

    private var job: Job = Job()

    private lateinit var functions: FirebaseFunctions
    private var notifyRecord:NotifyRecord?=null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleManager.setNewLocale(this, applicationContext, Preference.getAppLanguage())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mHandler = Handler()

        //check if the intent was from notification and its a update notification
        intent.extras?.let { bundle->
            val notifEvent: String? = bundle.getString("key", null)



            notifEvent?.let {
                if (it.equals(NotificationType.EVENT.type)) {
                    needToUpdateApp = true
                    intent = Intent(Intent.ACTION_VIEW);
                    //Copy App URL from Google Play Store.
                    intent.data = Uri.parse(BuildConfig.STORE_URL)

                    startActivity(intent)
                    finish()
                }else if(it.equals(NotificationType.INFECTION.type)){
                    notifyRecord = NotifyRecord()
                    Preference.putAnnouncement(this, true)
                    bundle.keySet().forEach {
                        when(it){
                            "title"-> notifyRecord?.title = bundle.getString("title")
                            "body" -> notifyRecord?.content = bundle.getString("body")
                            "sent_at" ->notifyRecord?.dateInfected = bundle.getString("sent_at")?.toLong()?.times(1000L)
                        }
                    }
                }
            }
        }



    }



    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        if (!needToUpdateApp) {
            mHandler.postDelayed({
                goToNextScreen()
                finish()
            }, SPLASH_TIME)
        }
    }

    override fun onStop() {
        super.onStop()
        job?.cancel()
    }

    private fun goToNextScreen() {
        if (!Preference.isOnBoarded(this)) {
            startActivity(Intent(this, PreOnboardingActivity::class.java))
        } else {
            launch {
                notifyRecord?.let {
                    val storage= NotifyRecordStorage(this@SplashActivity)
                    storage?.saveRecord(it)
                }
                if(BuildConfig.DEBUG){
                    Preference.putAnnouncement(this@SplashActivity, true)
                }


                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }

        }
    }
}
