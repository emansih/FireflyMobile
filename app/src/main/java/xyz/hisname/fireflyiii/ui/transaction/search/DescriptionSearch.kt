package xyz.hisname.fireflyiii.ui.transaction.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.dialog_search.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.base.BaseDialog
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel

class DescriptionSearch: BaseDialog() {

    private val descriptionAdapter by lazy { DescriptionAdapter { data: String -> itemClicked(data) } }
    private val descriptionViewModel by lazy { getViewModel(DescriptionViewModel::class.java) }
    private lateinit var initialData: PagingData<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.dialog_search, container)
    }


    private fun displayView(){
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recycler_view.adapter = descriptionAdapter
        descriptionViewModel.getAllDescription().observe(viewLifecycleOwner){ data ->
            initialData = data
            descriptionAdapter.submitData(lifecycle, data)
        }
        descriptionAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner) { loadStates ->
            swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
        }
    }

    private fun searchData(){
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean{
                descriptionViewModel.transactionName.postValue(query)
                dialog?.dismiss()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if(newText.isBlank() or newText.isEmpty()){
                    descriptionAdapter.submitData(lifecycle, initialData)
                } else {
                    descriptionViewModel.searchTransactionName(newText).observe(viewLifecycleOwner){ data ->
                        descriptionAdapter.submitData(lifecycle, data)
                    }
                }
                return true
            }

        })
    }

    private fun itemClicked(itemClicked: String){
        descriptionViewModel.transactionName.postValue(itemClicked)
        dialog?.dismiss()
    }

    override fun setWidgets() {
        displayView()
        searchData()
    }
}