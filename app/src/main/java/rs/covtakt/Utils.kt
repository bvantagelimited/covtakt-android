package rs.covtakt

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.HttpsCallableResult
import rs.covtakt.bluetooth.gatt.*
import rs.covtakt.logging.CentralLog
import rs.covtakt.scheduler.Scheduler
import rs.covtakt.services.BluetoothMonitoringService
import rs.covtakt.services.BluetoothMonitoringService.Companion.PENDING_ADVERTISE_REQ_CODE
import rs.covtakt.services.BluetoothMonitoringService.Companion.PENDING_BM_UPDATE
import rs.covtakt.services.BluetoothMonitoringService.Companion.PENDING_HEALTH_CHECK_CODE
import rs.covtakt.services.BluetoothMonitoringService.Companion.PENDING_PURGE_CODE
import rs.covtakt.services.BluetoothMonitoringService.Companion.PENDING_SCAN_REQ_CODE
import rs.covtakt.services.BluetoothMonitoringService.Companion.PENDING_START
import rs.covtakt.status.Status
import rs.covtakt.streetpass.ACTION_DEVICE_SCANNED
import rs.covtakt.streetpass.ConnectablePeripheral
import rs.covtakt.streetpass.ConnectionRecord
import rs.covtakt.util.FirebaseExceptionType
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*


object Utils {

    private const val TAG = "Utils"

    fun getRequiredPermissions(): Array<String> {
        return arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun getBatteryOptimizerExemptionIntent(packageName: String): Intent {
        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        return intent
    }

    fun canHandleIntent(batteryExemptionIntent: Intent, packageManager: PackageManager?): Boolean {
        packageManager?.let {
            return batteryExemptionIntent.resolveActivity(packageManager) != null
        }
        return false
    }

    fun getDate(milliSeconds: Long): String {
        val dateFormat = "dd/MM/yyyy HH:mm:ss.SSS"
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun getTime(milliSeconds: Long): String {
        val dateFormat = "h:mm a"
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun startBluetoothMonitoringService(context: Context) {
        val intent = Intent(context, BluetoothMonitoringService::class.java)
        intent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_START.index
        )

        context.startService(intent)
    }

    fun scheduleStartMonitoringService(context: Context, timeInMillis: Long) {
        val intent = Intent(context, BluetoothMonitoringService::class.java)
        intent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_START.index
        )

        Scheduler.scheduleServiceIntent(
            PENDING_START,
            context,
            intent,
            timeInMillis
        )
    }

    fun scheduleBMUpdateCheck(context: Context, bmCheckInterval: Long) {

        Utils.cancelBMUpdateCheck(context)

        val intent = Intent(context, BluetoothMonitoringService::class.java)
        intent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_UPDATE_BM.index
        )

        Scheduler.scheduleServiceIntent(
            PENDING_BM_UPDATE,
            context,
            intent,
            bmCheckInterval
        )
    }

    fun cancelBMUpdateCheck(context: Context) {
        val intent = Intent(context, BluetoothMonitoringService::class.java)
        intent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_UPDATE_BM.index
        )

        Scheduler.cancelServiceIntent(PENDING_BM_UPDATE, context, intent)
    }

    fun stopBluetoothMonitoringService(context: Context) {
        val intent = Intent(context, BluetoothMonitoringService::class.java)
        intent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_STOP.index
        )
        Utils.cancelNextScan(context)
        Utils.cancelNextHealthCheck(context)
        context.stopService(intent)
    }

    fun scheduleNextScan(context: Context, timeInMillis: Long) {

        //cancels any outstanding scan schedules.
        Utils.cancelNextScan(context)

        val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
        nextIntent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_SCAN.index
        )
        //runs every XXX milliseconds
        Scheduler.scheduleServiceIntent(
            PENDING_SCAN_REQ_CODE,
            context,
            nextIntent,
            timeInMillis
        )
    }

    fun cancelNextScan(context: Context) {
        val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
        nextIntent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_SCAN.index
        )
        Scheduler.cancelServiceIntent(PENDING_SCAN_REQ_CODE, context, nextIntent)
    }

    fun scheduleNextAdvertise(context: Context, timeToNextAdvertise: Long) {

        //cancels any outstanding scan schedules.
        Utils.cancelNextAdvertise(context)

        val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
        nextIntent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_ADVERTISE.index
        )
        //runs every XXX milliseconds
        Scheduler.scheduleServiceIntent(
            PENDING_ADVERTISE_REQ_CODE,
            context,
            nextIntent,
            timeToNextAdvertise
        )
    }

    fun cancelNextAdvertise(context: Context) {
        val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
        nextIntent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_ADVERTISE.index
        )
        Scheduler.cancelServiceIntent(PENDING_ADVERTISE_REQ_CODE, context, nextIntent)
    }

    fun scheduleNextHealthCheck(context: Context, timeInMillis: Long) {
        //cancels any outstanding check schedules.
        Utils.cancelNextHealthCheck(context)

        val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
        nextIntent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_SELF_CHECK.index
        )
        //runs every XXX milliseconds - every minute?
        Scheduler.scheduleServiceIntent(
            PENDING_HEALTH_CHECK_CODE,
            context,
            nextIntent,
            timeInMillis
        )
    }

    fun cancelNextHealthCheck(context: Context) {
        val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
        nextIntent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_SELF_CHECK.index
        )
        Scheduler.cancelServiceIntent(PENDING_HEALTH_CHECK_CODE, context, nextIntent)
    }

    fun scheduleRepeatingPurge(context: Context, intervalMillis: Long) {
        val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
        nextIntent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_PURGE.index
        )

        Scheduler.scheduleRepeatingServiceIntent(
            PENDING_PURGE_CODE,
            context,
            nextIntent,
            intervalMillis
        )
    }

    fun broadcastDeviceScanned(
        context: Context,
        device: BluetoothDevice,
        connectableBleDevice: ConnectablePeripheral
    ) {
        val intent = Intent(ACTION_DEVICE_SCANNED)
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
        intent.putExtra(CONNECTION_DATA, connectableBleDevice)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun broadcastDeviceProcessed(context: Context, deviceAddress: String) {
        val intent = Intent(ACTION_DEVICE_PROCESSED)
        intent.putExtra(DEVICE_ADDRESS, deviceAddress)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }


    fun broadcastStreetPassReceived(context: Context, streetpass: ConnectionRecord) {
        val intent = Intent(ACTION_RECEIVED_STREETPASS)
        intent.putExtra(STREET_PASS, streetpass)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun broadcastStatusReceived(context: Context, statusRecord: Status) {
        val intent = Intent(ACTION_RECEIVED_STATUS)
        intent.putExtra(STATUS, statusRecord)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun broadcastDeviceDisconnected(context: Context, device: BluetoothDevice) {
        val intent = Intent(ACTION_GATT_DISCONNECTED)
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun readFromInternalStorage(context: Context, fileName: String): String {
        CentralLog.d(Utils.TAG, "Reading from internal storage")
        val fileInputStream: FileInputStream
        var text: String? = null
        val stringBuilder: StringBuilder = StringBuilder()
        fileInputStream = context.openFileInput(fileName)
        var inputStreamReader: InputStreamReader = InputStreamReader(fileInputStream)
        val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)
        try {
            while ({ text = bufferedReader.readLine(); text }() != null) {
                CentralLog.d(Utils.TAG, "Text: " + text)
                stringBuilder.append(text)
            }

            bufferedReader.close()

        } catch (e: Throwable) {
            CentralLog.e(Utils.TAG, "Failed to readFromInternalStorage: ${e.message}")
        }
        return stringBuilder.toString()
    }

    fun getDateFromUnix(unix_timestamp: Long): String? {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH)
        val date = sdf.format(unix_timestamp)
        return date.toString()
    }

    fun hideKeyboardFrom(
        context: Context,
        view: View
    ) {
        val imm = context.getSystemService(
            Activity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboardFrom(
        context: Context,
        view: View?
    ) {
        val imm = context.getSystemService(
            Activity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }

    fun isBluetoothAvailable(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter != null &&
                bluetoothAdapter.isEnabled && bluetoothAdapter.state == BluetoothAdapter.STATE_ON
    }

    fun getHandShakePin(
        context: Context,
        functions: FirebaseFunctions
    ): Task<HttpsCallableResult> {
        return functions
            .getHttpsCallable("getHandshakePin")
            .call()
            .addOnSuccessListener {
                val result: HashMap<String, Any> = it.data as HashMap<String, Any>
                val handShakePin = result["pin"].toString()
                Preference.putHandShakePin(context, handShakePin)
                CentralLog.d(Utils.TAG, "Result from handshake pin: " + result.toString())
            }.addOnFailureListener { e ->
                CentralLog.w(Utils.TAG, "get handshake pin (failure): ${e.message}")
            }
    }
    fun setDeviceToken(
            token:String?,
            functions: FirebaseFunctions
    ): Task<HttpsCallableResult> {
        return functions
                .getHttpsCallable("setDeviceToken")
                .call(token)

    }

    fun getFirebaseExType(ex:FirebaseFunctionsException):FirebaseExceptionType = when(ex.code.name){
        FirebaseExceptionType.INVALID_ARGUMENT.type->FirebaseExceptionType.INVALID_ARGUMENT
        FirebaseExceptionType.FAILED_PRECONDITION.type->FirebaseExceptionType.FAILED_PRECONDITION
        else-> FirebaseExceptionType.UNKNOWN

    }


}