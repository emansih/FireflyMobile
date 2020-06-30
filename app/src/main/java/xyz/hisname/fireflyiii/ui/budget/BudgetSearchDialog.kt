package xyz.hisname.fireflyiii.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.dialog_search.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.budget.BudgetViewModel
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData
import xyz.hisname.fireflyiii.ui.base.BaseDialog
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel

class BudgetSearchDialog: BaseDialog() {

    private var dataAdapter = arrayListOf<BudgetListData>()
    private var initialAdapter = arrayListOf<BudgetListData>()
    private lateinit var budgetRecyclerAdapter: BudgetRecyclerAdapter
    private val budgetViewModel by lazy { getViewModel(BudgetViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.dialog_search, container)
    }


    private fun displayView(){
        recycler_view.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(),
                    LinearLayoutManager(requireContext()).orientation))
            adapter?.notifyDataSetChanged()
        }
        swipeContainer.isRefreshing = true
        budgetViewModel.retrieveAllBudgetLimits().observe(viewLifecycleOwner) { budgetData ->
            recycler_view.adapter = BudgetRecyclerAdapter(budgetData) { data: BudgetListData -> itemClicked(data) }
        }
        budgetViewModel.isLoading.observe(viewLifecycleOwner){ loading ->
            if (loading == false) {
                swipeContainer.isRefreshing = false
            }
        }
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
                if(newText.isBlank() or newText.isEmpty()){
                    budgetRecyclerAdapter = BudgetRecyclerAdapter(initialAdapter) { data: BudgetListData -> itemClicked(data) }
                    budgetRecyclerAdapter.notifyDataSetChanged()
                    recycler_view.adapter = budgetRecyclerAdapter
                } else {
                    filter(newText)
                }
                return true
            }

        })
    }


    private fun filter(budgetName: String){
        dataAdapter.clear()
        budgetViewModel.getBudgetByName(budgetName).observe(viewLifecycleOwner) { piggyData ->
            piggyData.forEachIndexed { _, catData ->
                dataAdapter.add(catData)
            }
            budgetRecyclerAdapter = BudgetRecyclerAdapter(dataAdapter) { data: BudgetListData ->  itemClicked(data)}
            budgetRecyclerAdapter.notifyDataSetChanged()
            recycler_view.adapter = budgetRecyclerAdapter
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