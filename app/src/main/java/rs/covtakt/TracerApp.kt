package rs.covtakt

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build

import rs.covtakt.idmanager.TempIDManager
import rs.covtakt.logging.CentralLog
import rs.covtakt.services.BluetoothMonitoringService
import rs.covtakt.streetpass.CentralDevice
import rs.covtakt.streetpass.PeripheralDevice
import rs.covtakt.util.DeviceUtils


class TracerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        TracerApp.Companion.AppContext = applicationContext
        initCrashLytics()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        LocaleManager.setNewLocale(this, applicationContext, Preference.getAppLanguage())
    }
    private fun initCrashLytics(){
//        if(!BuildConfig.DEBUG){
//            Fabric.with(this, Crashlytics())
//        }
    }
    companion object {
        var AppContext: Context? = null
        private val TAG = "TracerApp"


        internal fun getOrg(): String {
            if(TracerApp.Companion.AppContext == null){
                return ""
            }
            return  when(DeviceUtils.getDeviceCountryCode(TracerApp.Companion.AppContext!!)?.toUpperCase()){
               "HK" -> "HK_MOH"
                "SR" -> "RS_BAT"
                else -> "RS_BAT"
            }
        }





        fun thisDeviceMsg(): String {
            if(TracerApp.Companion.AppContext == null){
                return ""
            }
            BluetoothMonitoringService.broadcastMessage?.let {
                CentralLog.i(TracerApp.Companion.TAG, "Retrieved BM for storage: $it")

                if (!it.isValidForCurrentTime()) {

                    var fetch = TempIDManager.retrieveTemporaryID(TracerApp.Companion.AppContext!!)
                    fetch?.let {
                        CentralLog.i(TracerApp.Companion.TAG, "Grab New Temp ID")
                        BluetoothMonitoringService.broadcastMessage = it
                    }

                    if (fetch == null) {
                        CentralLog.e(TracerApp.Companion.TAG, "Failed to grab new Temp ID")
                    }

                }
            }
            return BluetoothMonitoringService.broadcastMessage?.tempID ?: "Missing TempID"
        }

        fun asPeripheralDevice(): PeripheralDevice {
            return PeripheralDevice(Build.MODEL, "SELF")
        }

        fun asCentralDevice(): CentralDevice {
            return CentralDevice(Build.MODEL, "SELF")
        }

    }
}
