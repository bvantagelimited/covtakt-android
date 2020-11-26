package rs.covtakt.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_notification.*
import rs.covtakt.R
import rs.covtakt.adapter.notification.NotificationLIstAdapt
import rs.covtakt.notifications.NotifyViewModel


class NotificationFragment  : BaseFragment() {
    private var privateContext: Context? = null
    private lateinit var inflater: LayoutInflater
    private lateinit var viewModel: NotifyViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        this.inflater = inflater
        val view = inflater.inflate(R.layout.fragment_notification, container, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    private fun initData(){
        val adapter = NotificationLIstAdapt(activity!!)
        recyclerview.adapter = adapter
        val layoutManager = LinearLayoutManager(activity)
        recyclerview.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
                recyclerview.context,
                layoutManager.orientation
        )
        recyclerview.addItemDecoration(dividerItemDecoration)

        viewModel = ViewModelProvider(this).get(NotifyViewModel::class.java)
        viewModel.allRecords.observe(viewLifecycleOwner, Observer { records ->
            if(records.isNotEmpty()){
                tvNoData.visibility = View.GONE
            }else{
                  tvNoData.visibility = View.VISIBLE
            }
            adapter.setData(records)
        })
    }

}
