package xyz.hisname.fireflyiii.ui.currency

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.display
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
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        initFab()
        pullToRefresh()
        displayView()
        currencyViewModel.isLoading.observe(this, Observer {
            swipeContainer.isRefreshing = it == true
        })
    }


    private fun displayView(){
        currencyViewModel.getCurrency().observe(this, Observer {currencyData ->
            dataAdapter = ArrayList(currencyData)
            currencyData.sortWith(Comparator { initial, after ->
                initial.currencyAttributes?.name!!.compareTo(after.currencyAttributes?.name!!)
            })
            recycler_view.adapter = CurrencyRecyclerAdapter(currencyData) { data: CurrencyData ->  }
        })
    }

    private fun initFab(){
        fab.display {
            fab.isClickable = false
            requireFragmentManager().commit {
                replace(R.id.bigger_fragment_container, AddCurrencyFragment())
                addToBackStack(null)
                arguments = bundleOf("revealX" to fab.width / 2, "revealY" to fab.height / 2)
            }
            fab.isClickable = true
        }
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dy > 0 && fab.isShown){
                    fab.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    fab.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            dataAdapter.clear()
            displayView()
        }
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = "Currency"
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = "Currency"
    }

    override fun onStop() {
        super.onStop()
        fab.isGone = true
    }

    override fun onDetach() {
        super.onDetach()
        fab.isGone = true
    }

}