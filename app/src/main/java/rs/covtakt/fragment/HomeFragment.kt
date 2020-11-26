package rs.covtakt.fragment

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.android.synthetic.main.fragment_home.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import rs.covtakt.*
import rs.covtakt.logging.CentralLog
import rs.covtakt.onboarding.OnboardingActivity
import rs.covtakt.status.persistence.StatusRecord
import rs.covtakt.streetpass.persistence.StreetPassRecordDatabase
import rs.covtakt.util.TextStyleUtils

private const val REQUEST_ENABLE_BT = 123
private const val PERMISSION_REQUEST_ACCESS_LOCATION = 456

class HomeFragment : BaseFragment() {
    private val TAG = "HomeFragment"

    private var mIsBroadcastListenerRegistered = false
    private var counter = 0

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var lastKnownScanningStarted: LiveData<StatusRecord?>

    internal var callback: HomeFragmentListener? = null

    fun setHomeListener(callback: HomeFragmentListener) {
        this.callback = callback

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = StreetPassRecordDatabase.getDatabase(view.context)

        lastKnownScanningStarted = db.statusDao().getMostRecentRecord("Scanning Started")
        lastKnownScanningStarted.observe(viewLifecycleOwner,
                Observer { record ->
                    if (record != null) {
                        tv_last_update.visibility = View.VISIBLE
                        // tv_last_update.text = "${context?.getString(R.string.last_update)} ${Utils.getTime(record.timestamp)}"
                        TextStyleUtils.setTime(tv_last_update, "${context?.getString(R.string.last_update)}",
                                "${Utils.getTime(record.timestamp)}")
                    }

                })

        showSetup()

        Preference.registerListener(activity!!.applicationContext, listener)
        linNoti.setOnClickListener {
            callback?.notiClick()
        }
        showNonEmptyAnnouncement()
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        Preference.registerListener(activity!!.applicationContext, listener)

        return view
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        share_card_view.setOnClickListener { shareThisApp() }
        animation_view.setOnClickListener {
            if (++counter == 2) {
                counter = 0
                var intent = Intent(context, PeekActivity::class.java)
                context?.startActivity(intent)
            }
        }
        btn_restart_app_setup.setOnClickListener {
            var intent = Intent(context, OnboardingActivity::class.java)
            intent.putExtra("page", 1)
            context?.startActivity(intent)
        }



        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(mapOf("ShareText" to getString(R.string.share_message)))
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(activity as Activity) { task ->
                    if (task.isSuccessful) {
                        val updated = task.result
                        CentralLog.d(TAG, "Remote config fetch - success: $updated")
                    } else {
                        CentralLog.d(TAG, "Remote config fetch - failed")
                    }
                }
    }

    private fun isShowRestartSetup(): Boolean {
        if (canRequestBatteryOptimizerExemption()) {
            if (iv_bluetooth.isSelected && iv_location.isSelected && iv_battery.isSelected) {
                return false
            }
        } else {
            if (iv_bluetooth.isSelected && iv_location.isSelected) {
                return false
            }
        }
        if (iv_bluetooth.isSelected) {
            val status = getString(R.string.bluetooth_on) + " ${resources.getString(R.string.status_on)}"

            tv_bluetooth_status.setText(status)
        } else {
            val status = getString(R.string.bluetooth_on) + " ${resources.getString(R.string.status_off)}"

            tv_bluetooth_status.setText(status)

        }
        if (iv_location.isSelected) {
            val status = getString(R.string.location_on) + " ${resources.getString(R.string.status_on)}"
            tv_location_status.setText(status)

        } else {
            val status = getString(R.string.location_on) + " ${resources.getString(R.string.status_off)}"

            tv_location_status.setText(status)

        }
        if (iv_battery.isSelected) {
            val status = getString(R.string.battery_optimiser) + " ${resources.getString(R.string.status_on)}"

            tv_battery_status.setText(status)

        } else {
            val status = getString(R.string.battery_optimiser) + " ${resources.getString(R.string.status_off)}"

            tv_battery_status.setText(status)

        }
        return true
    }

    private fun canRequestBatteryOptimizerExemption(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Utils.canHandleIntent(
                Utils.getBatteryOptimizerExemptionIntent(
                        TracerApp.AppContext!!.packageName
                ), TracerApp.AppContext!!.packageManager
        )
    }

    fun showSetup() {
        view_setup.isVisible = isShowRestartSetup()
        view_complete.isVisible = !isShowRestartSetup()
    }

    override fun onResume() {
        super.onResume()
        if (!mIsBroadcastListenerRegistered) {
            // bluetooth on/off
            var f = IntentFilter()
            f.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            activity!!.registerReceiver(mBroadcastListener, f)
            mIsBroadcastListenerRegistered = true
        }

        view?.let {
            //location permission
            val perms = Utils.getRequiredPermissions()
            iv_location.isSelected =
                    EasyPermissions.hasPermissions(activity as MainActivity, *perms)

            //push notification
            iv_push.isSelected =
                    NotificationManagerCompat.from(activity as MainActivity).areNotificationsEnabled()

            bluetoothAdapter?.let {
                iv_bluetooth.isSelected = !it.isDisabled
            }

            //battery ignore list
            val powerManager =
                    (activity as MainActivity).getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
            val packageName = (activity as MainActivity).packageName

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                battery_card_view.visibility = View.VISIBLE
                if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                    iv_battery.isSelected = false
                    CentralLog.d(TAG, "Not on Battery Optimization whitelist")
                } else {
                    iv_battery.isSelected = true
                    CentralLog.d(TAG, "On Battery Optimization whitelist")
                }
            } else {
                battery_card_view.visibility = View.GONE
            }

            showSetup()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mIsBroadcastListenerRegistered) {
            activity!!.unregisterReceiver(mBroadcastListener)
            mIsBroadcastListenerRegistered = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Preference.unregisterListener(activity!!.applicationContext, listener)
        lastKnownScanningStarted.removeObservers(viewLifecycleOwner)
    }

    private fun shareThisApp() {
        var newIntent = Intent(Intent.ACTION_SEND)
        newIntent.type = "text/plain"
        newIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        var shareMessage = remoteConfig.getString("ShareText")
        newIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(newIntent, "choose one"))
    }

    private val mBroadcastListener: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                var state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                if (state == BluetoothAdapter.STATE_OFF) {
                    iv_bluetooth.isSelected = false
                } else if (state == BluetoothAdapter.STATE_TURNING_OFF) {
                    iv_bluetooth.isSelected = false
                } else if (state == BluetoothAdapter.STATE_ON) {
                    iv_bluetooth.isSelected = true
                }

                showSetup()
            }
        }
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
                (activity as MainActivity).getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private fun enableBluetooth() {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        bluetoothAdapter?.let {
            if (it.isDisabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
    }

    @AfterPermissionGranted(PERMISSION_REQUEST_ACCESS_LOCATION)
    fun setupPermissionsAndSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val perms = Utils.getRequiredPermissions()
            if (EasyPermissions.hasPermissions(activity as MainActivity, *perms)) {
                // Already have permission, do the thing
            } else {
                // Do not have permissions, request them now
                EasyPermissions.requestPermissions(
                        this, getString(R.string.permission_location_rationale),
                        PERMISSION_REQUEST_ACCESS_LOCATION, *perms
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT) {
            iv_bluetooth.isSelected = resultCode == Activity.RESULT_OK
        }
        showSetup()
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CentralLog.d(TAG, "[onRequestPermissionsResult]requestCode $requestCode")
        when (requestCode) {
            PERMISSION_REQUEST_ACCESS_LOCATION -> {
                iv_location.isSelected = permissions.isNotEmpty()
            }
        }

        showSetup()
    }

    private var listener: SharedPreferences.OnSharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    "ANNOUNCEMENT_NOTI" -> showNonEmptyAnnouncement()
                }
            }


    private fun showNonEmptyAnnouncement() {
        val new = Preference.getAnnouncement(activity!!.applicationContext)
        if (!new) {
            linNoti.isVisible = false
            return
        }

        linNoti.isVisible = true
    }

    interface HomeFragmentListener {
        fun notiClick()
    }
}