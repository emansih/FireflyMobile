package xyz.hisname.fireflyiii.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.dialog_search.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.budget.BudgetViewModel
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData
import xyz.hisname.fireflyiii.ui.base.BaseDialog
import xyz.hisname.fireflyiii.util.EndlessRecyclerViewScrollListener
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel

class BudgetSearchDialog: BaseDialog() {

    private var dataAdapter = arrayListOf<BudgetListData>()
    private val budgetViewModel by lazy { getViewModel(BudgetViewModel::class.java) }
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private val budgetAdapter by lazy { BudgetRecyclerAdapter(dataAdapter){ data: BudgetListData -> itemClicked(data) } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.dialog_search, container)
    }


    private fun displayView(){
        val linearLayoutManager = LinearLayoutManager(requireContext())
        recycler_view.apply {
            layoutManager = linearLayoutManager
            addItemDecoration(DividerItemDecoration(requireContext(),
                    LinearLayoutManager(requireContext()).orientation))
            adapter = budgetAdapter
        }
        swipeContainer.isRefreshing = true
        budgetViewModel.retrieveAllBudgetLimits(1).observe(viewLifecycleOwner) { budgetData ->
            dataAdapter.addAll(budgetData)
            budgetAdapter.update(dataAdapter)
            budgetAdapter.notifyDataSetChanged()
        }
        budgetViewModel.isLoading.observe(viewLifecycleOwner){ loading ->
            if (loading == false) {
                swipeContainer.isRefreshing = false
            }
        }
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                if(!swipeContainer.isRefreshing) {
                    swipeContainer.isRefreshing = true
                    budgetViewModel.retrieveAllBudgetLimits(page + 1).observe(viewLifecycleOwner){ budgetData ->
                        dataAdapter.clear()
                        dataAdapter.addAll(budgetData)
                        budgetAdapter.update(budgetData)
                        budgetAdapter.notifyDataSetChanged()
                        swipeContainer.isRefreshing = false

                    }
                }
            }
        }
        recycler_view.addOnScrollListener(scrollListener)
    }

    private fun itemClicked(budgetData: BudgetListData){
        budgetViewModel.postBudgetName(budgetData.budgetListAttributes?.name)
        dialog?.dismiss()
    }


    private fun searchData(){
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean{
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return true
            }

        })
    }


    private fun filter(budgetName: String){
        dataAdapter.clear()
        budgetViewModel.getBudgetByName(budgetName).observe(viewLifecycleOwner) { budgetData ->
            budgetData.forEach { budget ->
                dataAdapter.add(budget)
            }
            dataAdapter.clear()
            dataAdapter.addAll(budgetData)
            budgetAdapter.update(budgetData)
            budgetAdapter.notifyDataSetChanged()
        }
    }

    override fun setIcons() {

    }

    override fun setWidgets() {
        displayView()
        searchData()
    }

    override fun submitData() {
    }
}