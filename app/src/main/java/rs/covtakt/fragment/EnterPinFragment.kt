package rs.covtakt.fragment

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_upload_enterpin.*
import rs.covtakt.BuildConfig
import rs.covtakt.R
import rs.covtakt.TracerApp
import rs.covtakt.Utils
import rs.covtakt.logging.CentralLog
import rs.covtakt.status.persistence.StatusRecord
import rs.covtakt.status.persistence.StatusRecordStorage
import rs.covtakt.streetpass.persistence.StreetPassRecord
import rs.covtakt.streetpass.persistence.StreetPassRecordStorage
import rs.covtakt.util.FirebaseExceptionType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class EnterPinFragment : BaseFragment() {
    private var TAG = "UploadFragment"

    private var disposeObj: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_enterpin, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        enterPinFragmentUploadCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.length == 6) {
                    Utils.hideKeyboardFrom(view.context, view)
                }
            }
        })

        enterPinActionButton.setOnClickListener {
            enterPinFragmentErrorMessage.visibility = View.GONE
            var myParentFragment: UploadPageFragment = (parentFragment as UploadPageFragment)
            myParentFragment.turnOnLoadingProgress()

            var observableStreetRecords = Observable.create<List<StreetPassRecord>> {
                val result = StreetPassRecordStorage(TracerApp.AppContext!!).getAllRecords()
                it.onNext(result)
            }
            var observableStatusRecords = Observable.create<List<StatusRecord>> {
                val result = StatusRecordStorage(TracerApp.AppContext!!).getAllRecords()
                it.onNext(result)
            }

            disposeObj = Observable.zip(observableStreetRecords, observableStatusRecords,

                BiFunction<List<StreetPassRecord>, List<StatusRecord>, ExportData> { records, status ->
                    ExportData(
                        records,
                        status
                    )
                }

            )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe { exportedData ->
                    Log.d(TAG, "records: ${exportedData.recordList}")
                    Log.d(TAG, "status: ${exportedData.statusList}")
                   var pin = enterPinFragmentUploadCode.text.toString()
                    getUploadToken(pin).addOnSuccessListener {
                        val response = it.data as HashMap<String, String>
                        try {
                            val uploadToken = response["token"]
                            CentralLog.d(TAG, "uploadToken: $uploadToken")
                            var task = writeToInternalStorageAndUpload(
                                TracerApp.AppContext!!,
                                exportedData.recordList,
                                exportedData.statusList,
                                uploadToken
                            )
                            task.addOnFailureListener {
                                CentralLog.d(TAG, "failed to upload")
                                var myParentFragment: UploadPageFragment =
                                    (parentFragment as UploadPageFragment)
                                myParentFragment.turnOffLoadingProgress()
                                enterPinFragmentErrorMessage.visibility = View.VISIBLE
                            }.addOnSuccessListener {
                                CentralLog.d(TAG, "uploaded successfully")
                                var myParentFragment: UploadPageFragment =
                                    (parentFragment as UploadPageFragment)
                                myParentFragment.turnOffLoadingProgress()
                                myParentFragment.navigateToUploadComplete()
                            }
                        } catch (e: Throwable) {
                            CentralLog.d(TAG, "Failed to upload data: ${e.message}")
                            var myParentFragment: UploadPageFragment =
                                (parentFragment as UploadPageFragment)
                            myParentFragment.turnOffLoadingProgress()
                            enterPinFragmentErrorMessage.visibility = View.VISIBLE
                        }
                    }.addOnFailureListener {
                        CentralLog.d(TAG, "Invalid code")
                        var myParentFragment: UploadPageFragment =
                            (parentFragment as UploadPageFragment)
                        myParentFragment.turnOffLoadingProgress()

                            showFirebaseExMessage(it)
                      //  enterPinFragmentErrorMessage.visibility = View.VISIBLE

                    }
                }
        }

        enterPinFragmentBackButtonLayout.setOnClickListener {
            println("onclick is pressed")
            var myParentFragment: UploadPageFragment = (parentFragment as UploadPageFragment)
            myParentFragment.popStack()
        }

        enterPinFragmentBackButton.setOnClickListener {
            println("onclick is pressed")
            var myParentFragment: UploadPageFragment = (parentFragment as UploadPageFragment)
            myParentFragment.popStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeObj?.dispose()
    }

    private fun getUploadToken(uploadCode: String): Task<HttpsCallableResult> {

        val functions = FirebaseFunctions.getInstance(BuildConfig.FIREBASE_REGION)
        return functions
            .getHttpsCallable("getUploadToken")
            .call(uploadCode)
    }

    private fun writeToInternalStorageAndUpload(
        context: Context,
        deviceDataList: List<StreetPassRecord>,
        statusList: List<StatusRecord>,
        uploadToken: String?
    ): UploadTask {
        var date = Utils.getDateFromUnix(System.currentTimeMillis())
        var gson = Gson()

        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL

        var updatedDeviceList = deviceDataList.map {
            it.timestamp = it.timestamp / 1000
            return@map it
        }

        var updatedStatusList = statusList.map {
            it.timestamp = it.timestamp / 1000
            return@map it
        }

        var map: MutableMap<String, Any> = HashMap()
        map["token"] = uploadToken as Any
        map["records"] = updatedDeviceList as Any
        map["events"] = updatedStatusList as Any

        val mapString = gson.toJson(map)

        val fileName = "${manufacturer}_${model}_$date.json"
        val fileOutputStream: FileOutputStream

        val uploadDir = File(context.filesDir, "upload")

        if (uploadDir.exists()) {
            uploadDir.deleteRecursively()
        }

        uploadDir.mkdirs()
        val fileToUpload = File(uploadDir, fileName)
//        fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        fileOutputStream = FileOutputStream(fileToUpload)

        fileOutputStream.write(mapString.toByteArray())
        fileOutputStream.close()

        CentralLog.i(TAG, "File wrote: ${fileToUpload.absolutePath}")

        return uploadToCloudStorage(context, fileToUpload)
    }

    private fun uploadToCloudStorage(context: Context, fileToUpload: File): UploadTask {
        CentralLog.d(TAG, "Uploading to Cloud Storage")

        val bucketName = BuildConfig.FIREBASE_UPLOAD_BUCKET
        val storage = FirebaseStorage.getInstance("gs://${bucketName}")
        var storageRef = storage.getReferenceFromUrl("gs://${bucketName}")

        val dateString = SimpleDateFormat("yyyyMMdd").format(Date())
       // val pin =Preference.getHandShakePin(context)
        var streetPassRecordsRef =
            storageRef.child("streetPassRecords/$dateString/${fileToUpload.name}")

        val fileUri: Uri =
            FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                fileToUpload
            )

        var uploadTask = streetPassRecordsRef.putFile(fileUri)
        uploadTask.addOnCompleteListener {
            try {
                fileToUpload.delete()
                CentralLog.i(TAG, "upload file deleted")
            } catch (e: Exception) {
                CentralLog.e(TAG, "Failed to delete upload file")
            }
        }
        return uploadTask
    }

    private fun alertDialog(desc: String?) {
        val dialogBuilder = AlertDialog.Builder(activity)
        dialogBuilder.setMessage(desc)
                .setCancelable(true)
                .setPositiveButton(
                        getString(R.string.ok)
                ) { dialog, id ->

                    dialog.dismiss()
                }

        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun showFirebaseExMessage(type:Exception){
        val result = type as FirebaseFunctionsException
        when(Utils.getFirebaseExType(result)){
            FirebaseExceptionType.INVALID_ARGUMENT->{
                enterPinFragmentErrorMessage.visibility = View.VISIBLE
            }
            FirebaseExceptionType.FAILED_PRECONDITION->{
                enterPinFragmentErrorMessage.visibility = View.GONE
                alertDialog(result.message)
            }
        }
    }

}
