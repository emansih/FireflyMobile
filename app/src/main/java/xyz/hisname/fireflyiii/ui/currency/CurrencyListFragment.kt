package xyz.hisname.fireflyiii.ui.currency

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.display
import xyz.hisname.fireflyiii.util.extension.hideFab
import xyz.hisname.fireflyiii.util.extension.hideKeyboard

class CurrencyListFragment: BaseFragment() {

    private var dataAdapter = arrayListOf<CurrencyData>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.base_swipe_layout, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboard()
        runLayoutAnimation(recycler_view)
        initFab()
        pullToRefresh()
        displayView()
        currencyViewModel.isLoading.observe(this) {
            swipeContainer.isRefreshing = it == true
        }
    }


    private fun displayView(){
        currencyViewModel.getCurrency().observe(this) {currencyData ->
            dataAdapter = ArrayList(currencyData)
            recycler_view.adapter = CurrencyRecyclerAdapter(dataAdapter) { data: CurrencyData ->
                clickListener(data)
            }.apply {
                update(dataAdapter)
            }
        }
    }

    private fun clickListener(data: CurrencyData){
        parentFragmentManager.commit {
            replace(R.id.bigger_fragment_container, AddCurrencyFragment().apply {
                arguments = bundleOf("currencyId" to data.currencyId)
            })
            addToBackStack(null)
        }
    }

    private fun initFab(){
        fab.display {
            fab.isClickable = false
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, AddCurrencyFragment().apply {
                    arguments = bundleOf("revealX" to fab.width / 2, "revealY" to fab.height / 2)
                })
                addToBackStack(null)
            }
            fab.isClickable = true
        }
        recycler_view.hideFab(fab)
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            dataAdapter.clear()
            displayView()
        }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.currency)
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = resources.getString(R.string.currency)
    }

    override fun onStop() {
        super.onStop()
        fab.isGone = true
    }

    override fun onDetach() {
        super.onDetach()
        fab.isGone = true
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}