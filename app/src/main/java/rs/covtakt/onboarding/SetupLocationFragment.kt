package rs.covtakt.onboarding

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import rs.covtakt.R
import rs.covtakt.logging.CentralLog
import com.google.firebase.analytics.FirebaseAnalytics

class SetupLocationFragment : OnboardingFragmentInterface() {
    private var listener: OnFragmentInteractionListener? = null
    private val TAG: String = "SetupCompleteFragment"
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var mainContext: Context

    override fun getButtonText(): String = context?.getString(R.string.continue_)!!

    override fun becomesVisible() {}

    override fun onButtonClick(view: View) {
        CentralLog.d(TAG, "OnButtonClick 4")
        val onboardActivity = context as OnboardingActivity
       // onboardActivity.navigateToNextPage()

        onboardActivity.setupPermissionsAndSettings()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        firebaseAnalytics = FirebaseAnalytics.getInstance(mainContext)
        return inflater.inflate(R.layout.fragment_setup_location, container, false)
    }

    override fun getProgressValue(): Int = 64

    override fun onUpdatePhoneNumber(num: String) {}

    override fun onError(error: String) {}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainContext = context;
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}