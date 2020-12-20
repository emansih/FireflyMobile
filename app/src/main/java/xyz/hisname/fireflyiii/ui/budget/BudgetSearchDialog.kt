package xyz.hisname.fireflyiii.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.dialog_search.*
import kotlinx.android.synthetic.main.fragment_base_list.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData
import xyz.hisname.fireflyiii.ui.base.BaseDialog
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel

class BudgetSearchDialog: BaseDialog() {

    private val budgetAdapter by lazy { BudgetRecyclerAdapter { data: BudgetListData -> itemClicked(data) } }
    private val budgetSearchViewModel by lazy { getViewModel(BudgetSearchViewModel::class.java) }
    private lateinit var initialBudget: PagingData<BudgetListData>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.dialog_search, container)
    }


    private fun displayView(){
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recycler_view.adapter = budgetAdapter
        budgetSearchViewModel.getAllBudget().observe(viewLifecycleOwner){ pagingData ->
            initialBudget = pagingData
            budgetAdapter.submitData(lifecycle, pagingData)
        }
        budgetAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner){ loadStates ->
            swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
        }
    }

    private fun itemClicked(budgetData: BudgetListData){
        budgetSearchViewModel.budgetName.postValue(budgetData.budgetListAttributes.name)
        dialog?.dismiss()
    }


    private fun searchData(){
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean{
                budgetSearchViewModel.budgetName.postValue(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if(newText.isEmpty()){
                    budgetAdapter.submitData(lifecycle, initialBudget)
                } else {
                    budgetSearchViewModel.searchBudget(newText).observe(viewLifecycleOwner){ pagingData ->
                        budgetAdapter.submitData(lifecycle, pagingData)
                    }
                }
                return true
            }

        })
    }

    override fun setWidgets() {
        displayView()
        searchData()
    }

}