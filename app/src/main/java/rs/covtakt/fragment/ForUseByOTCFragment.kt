package rs.covtakt.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import rs.covtakt.MainActivity
import rs.covtakt.R

class ForUseByOTCFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forusebyotc, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val childFragMan: FragmentManager = childFragmentManager
        val childFragTrans: FragmentTransaction = childFragMan.beginTransaction()
        val fragB = ForUseFragment()
        childFragTrans.add(R.id.fragment_placeholder, fragB)
        childFragTrans.addToBackStack("VerifyCaller")
        childFragTrans.commit()
    }

    fun goToUploadFragment() {
        val parentActivity: MainActivity = activity as MainActivity
        parentActivity.openFragment(
            parentActivity.LAYOUT_MAIN_ID,
            UploadPageFragment(),
            UploadPageFragment::class.java.name,
            0
        )
    }
}
