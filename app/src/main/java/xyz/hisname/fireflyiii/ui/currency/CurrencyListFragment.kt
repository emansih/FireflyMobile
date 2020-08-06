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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.currency.CurrencyViewModel
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.EndlessRecyclerViewScrollListener
import xyz.hisname.fireflyiii.util.extension.*

class CurrencyListFragment: BaseFragment() {

    private var dataAdapter = arrayListOf<CurrencyData>()
    private val currencyAdapter by lazy { CurrencyRecyclerAdapter(dataAdapter) { data: CurrencyData ->
        clickListener(data) }
    }
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private val currencyViewModel by lazy { getImprovedViewModel(CurrencyViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.base_swipe_layout, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboard()
        recycler_view.layoutManager = linearLayout()
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recycler_view.adapter = currencyAdapter
        initFab()
        pullToRefresh()
        displayView()
        currencyViewModel.isLoading.observe(viewLifecycleOwner) {
            swipeContainer.isRefreshing = it == true
        }
    }


    private fun displayView(){
        currencyViewModel.getCurrency(1).observe(viewLifecycleOwner) { currencyData ->
            dataAdapter.addAll(currencyData)
            currencyAdapter.update(dataAdapter)
            currencyAdapter.notifyDataSetChanged()
        }
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayout()){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                // Don't load more when data is refreshing
                if(!swipeContainer.isRefreshing) {
                    currencyViewModel.getCurrency(page + 1).observe(viewLifecycleOwner) { currencyList ->
                        dataAdapter.clear()
                        dataAdapter.addAll(currencyList)
                        currencyAdapter.update(currencyList)
                        currencyAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
        recycler_view.addOnScrollListener(scrollListener)
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
        extendedFab.display {
            extendedFab.isClickable = false
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, AddCurrencyFragment().apply {
                    arguments = bundleOf("revealX" to extendedFab.width / 2, "revealY" to extendedFab.height / 2)
                })
                addToBackStack(null)
            }
            extendedFab.isClickable = true
        }
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            scrollListener.resetState()
            dataAdapter.clear()
            currencyAdapter.notifyDataSetChanged()
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
        extendedFab.isGone = true
    }

    override fun onDetach() {
        super.onDetach()
        extendedFab.isGone = true
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}