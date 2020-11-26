package rs.covtakt

import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main_new.*
import rs.covtakt.fragment.ForUseByOTCFragment
import rs.covtakt.fragment.HomeFragment
import rs.covtakt.fragment.NotificationFragment
import rs.covtakt.logging.CentralLog
import rs.covtakt.notifications.NotifyViewModel
import rs.covtakt.util.ToastUtils

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    // navigation
    private var mNavigationLevel = 0
    var LAYOUT_MAIN_ID = 0
    private var selected = 0
    private val functions = FirebaseFunctions.getInstance(BuildConfig.FIREBASE_REGION)
    private lateinit var viewModel: NotifyViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleManager.setNewLocale(this, applicationContext, Preference.getAppLanguage())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_new)

        Utils.startBluetoothMonitoringService(this)

        LAYOUT_MAIN_ID = R.id.content

        initBottomBar()
        goToHome()

        getFCMToken()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleManager.setNewLocale(this, applicationContext, Preference.getAppLanguage())
    }

    private fun getFCMToken() {
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        CentralLog.w(TAG, "failed to get fcm token ${task.exception}")
                        return@addOnCompleteListener
                    } else {
                        // Get new Instance ID token
                        val token = task.result?.token
                        val useId = FirebaseAuth.getInstance().currentUser?.uid
                        if (useId != null) {
                            Utils.setDeviceToken(token, functions).addOnSuccessListener {

                                CentralLog.d(TAG, "Set Device token success")
                            }.addOnFailureListener { e ->
                                CentralLog.w(TAG, "Set Device token (failure): ${e.message}")
                                ToastUtils.showLong(this, e.message)
                            }
                        }



                        CentralLog.d(TAG, "FCM token: $token")
                    }
                }.addOnFailureListener {
                    ToastUtils.showLong(this, it.message)
                }


    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun goToHome() {
        nav_view.selectedItemId = R.id.navigation_home
    }

    fun openFragment(
            containerViewId: Int,
            fragment: Fragment,
            tag: String,
            title: Int
    ) {
        try { // pop all fragments
            supportFragmentManager.popBackStackImmediate(
                    LAYOUT_MAIN_ID,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            mNavigationLevel = 0
            val transaction =
                    supportFragmentManager.beginTransaction()
            transaction.replace(containerViewId, fragment, tag)
            transaction.commit()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun initBottomBar() {
        val mOnNavigationItemSelectedListener =
                BottomNavigationView.OnNavigationItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.navigation_home -> {

                            if (selected != R.id.navigation_home) {
                                val homeFragment = HomeFragment()
                                homeFragment.setHomeListener(object : HomeFragment.HomeFragmentListener {
                                    override fun notiClick() {
                                        nav_view.selectedItemId = R.id.navigation_help


                                    }
                                })

                                openFragment(
                                        LAYOUT_MAIN_ID, homeFragment,
                                        HomeFragment::class.java.name, 0
                                )
                            }
                            selected = R.id.navigation_home
                            return@OnNavigationItemSelectedListener true
                        }
                        R.id.navigation_upload -> {
                            if (selected != R.id.navigation_upload) {
                                openFragment(
                                        LAYOUT_MAIN_ID, ForUseByOTCFragment(),
                                        ForUseByOTCFragment::class.java.name, 0
                                )
                            }

                            selected = R.id.navigation_upload
                            return@OnNavigationItemSelectedListener true
                        }
                        R.id.navigation_help -> {
                            // Toast.makeText(this, "To be implemented", Toast.LENGTH_LONG).show()
                            if (selected != R.id.navigation_help) {
                                openFragment(
                                        LAYOUT_MAIN_ID, NotificationFragment(),
                                        NotificationFragment::class.java.name, 0
                                )
                            }


                            selected = R.id.navigation_help


                            return@OnNavigationItemSelectedListener true
                        }
                    }
                    false
                }

        nav_view.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        viewModel = ViewModelProvider(this).get(NotifyViewModel::class.java)
        viewModel.allRecords.observe(this, Observer { records ->
            if (records.isNotEmpty()) {
                nav_view.getOrCreateBadge(R.id.navigation_help)
            } else {
                if (Preference.getAnnouncement(this)) {
                    nav_view.removeBadge(R.id.navigation_help)
                }
            }
        })

    }

}
