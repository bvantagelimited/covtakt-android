package rs.covtakt.onboarding

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.analytics.FirebaseAnalytics
import rs.covtakt.R

class SetupBatteryFragment : OnboardingFragmentInterface() {
    private var listener: OnFragmentInteractionListener? = null
    private val TAG: String = "SetupCompleteFragment"
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var mainContext: Context

    override fun getButtonText(): String = context?.getString(R.string.ok)!!

    override fun becomesVisible() {}

    override fun onButtonClick(view: View) {
        val activity = context as OnboardingActivity?
        activity?.excludeFromBatteryOptimization()


    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        firebaseAnalytics = FirebaseAnalytics.getInstance(mainContext)
        return inflater.inflate(R.layout.fragment_setup_battery, container, false)
    }

    override fun getProgressValue(): Int = 100

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