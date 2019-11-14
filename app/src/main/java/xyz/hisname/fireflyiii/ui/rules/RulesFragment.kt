package xyz.hisname.fireflyiii.ui.rules

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.models.rules.RulesData
import xyz.hisname.fireflyiii.repository.viewmodel.RulesViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel

class RulesFragment: BaseFragment() {

    private var dataAdapter = ArrayList<RulesData>()
    private val model: RulesViewModel by lazy { getViewModel(RulesViewModel::class.java)}


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.base_swipe_layout, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showRules()
        pullToRefresh()
    }

    private fun showRules(){
        swipeContainer.isRefreshing = true
        runLayoutAnimation(recycler_view)
        model.getAllRules().observe(this, Observer {
            if(it.getError() == null){
                recycler_view.adapter = RulesRecyclerAdapter(it.getRules()?.data!!.toMutableList())
            }
            swipeContainer.isRefreshing = false
        })

    }

    private fun pullToRefresh(){
        dataAdapter.clear()
        swipeContainer.setOnRefreshListener {
            showRules()
        }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = "Rules"
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = "Rules"
    }

    override fun handleBack() {
        requireParentFragment().parentFragmentManager.popBackStack()
    }
}