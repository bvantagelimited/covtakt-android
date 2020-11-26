package rs.covtakt.onboarding

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.android.synthetic.main.main_activity_howitworks.*
import rs.covtakt.*
import rs.covtakt.logging.CentralLog


class HowItWorksActivity : FragmentActivity() {

    private var isFirebaseLoginSuccess: Boolean = false
    private val COVERAGE_API = "https://stage.ipification.com/auth/realms/ipification/coverage"
    private val functions = FirebaseFunctions.getInstance(BuildConfig.FIREBASE_REGION)
    private var TAG: String = "HowItWorksActivity"
    private var errorMessage :String?=null
    private var isInternetON = true
    private var errorNetWork = false

    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleManager.setNewLocale(this, applicationContext, Preference.getAppLanguage())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_howitworks)

      //  doCheckCoverage()
        doAnonymouslySign(FirebaseAuth.getInstance())

        btn_onboardingStart.setOnClickListener {
            if(frameProgress.visibility==View.VISIBLE) return@setOnClickListener
            if(errorNetWork){
                doAnonymouslySign(FirebaseAuth.getInstance())
                return@setOnClickListener
            }
            if(!isFirebaseLoginSuccess) {
                alertDialog(errorMessage)
                return@setOnClickListener
            }
           goNextPage()
        }
        btn_onboardingStart.setOnLongClickListener(object: View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {

                return false
            }

        })
        privacy.setMovementMethod(LinkMovementMethod.getInstance());
//        privacy.setOnClickListener {
//            val intent = Intent(Intent.ACTION_VIEW);
//            intent.data = Uri.parse("google.com")
//
//            startActivity(intent)
//        }
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleManager.setNewLocale(this, applicationContext, Preference.getAppLanguage())
    }
    fun doAnonymouslySign(auth: FirebaseAuth){
        frameProgress.visibility = View.VISIBLE

//        if(auth.currentUser!=null) {
//           // getHandShakePin()
//            return
//        }

        auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information


                        btn_onboardingStart.isEnabled = true
                        btn_onboardingStart.text = getString(R.string.great)

                        errorMessage = null
                        isFirebaseLoginSuccess = true
                        frameProgress.visibility = View.GONE
                        if(errorNetWork){
                            errorNetWork = false
                            goNextPage()
                        }
                        isInternetON = true

                    } else {
                        // If sign in fails, display a message to the user.
                        btn_onboardingStart.isEnabled = true
                        btn_onboardingStart.text = getString(R.string.great)
                        errorMessage = task.exception?.message
                        frameProgress.visibility = View.GONE
                        isInternetON = true



                    }

                    // ...
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    btn_onboardingStart.isEnabled = true
                    btn_onboardingStart.text = getString(R.string.great)
                    isFirebaseLoginSuccess = false
                    errorMessage = it.message
                    frameProgress.visibility = View.GONE
                    if(errorNetWork){
                        alertDialog(errorMessage)
                    }
                    if(it is FirebaseNetworkException){
                        isInternetON = false
                    }
                }

    }

    private fun getHandShakePin(){
        Utils.getHandShakePin(this, functions).addOnCompleteListener {
            if (it.isSuccessful) {
                CentralLog.d(TAG, "Retrieved HandShakePin successfully")
                errorMessage = null
                isFirebaseLoginSuccess = true
                frameProgress.visibility = View.GONE
                if(errorNetWork){
                    errorNetWork = false
                    goNextPage()
                }
                isInternetON = true
            } else {
                CentralLog.e(
                        TAG,
                        "Failed to retrieve HandShakePin ${it.exception?.message}"
                )
                errorMessage = "Failed to retrieve HandShakePin ${it.exception?.message}"
                isFirebaseLoginSuccess = false
                frameProgress.visibility = View.GONE
                isInternetON = true

            }
        }.addOnFailureListener {
            isFirebaseLoginSuccess = false
            errorMessage = "Failed to retrieve HandShakePin ${it?.message}"
            frameProgress.visibility = View.GONE
            if(errorNetWork){
                alertDialog(errorMessage)
            }
            if(it is FirebaseFunctionsException|| it is FirebaseNetworkException){
                isInternetON = false
            }
        }
    }

    private fun alertDialog(desc: String?) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(desc)
                .setCancelable(false)
                .setPositiveButton(
                        getString(R.string.ok),
                        DialogInterface.OnClickListener { dialog, id ->
                           errorNetWork = !isInternetON
                            dialog.dismiss()
                        })

        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun goNextPage(){
        val b: Bundle = Bundle()
        b.putBoolean("firebaseLogin", isFirebaseLoginSuccess);
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.putExtras(b)
        startActivity(intent)
    }

}
