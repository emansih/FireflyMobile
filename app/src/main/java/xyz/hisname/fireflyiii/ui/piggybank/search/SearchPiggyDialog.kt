package xyz.hisname.fireflyiii.ui.piggybank.search

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
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.ui.base.BaseDialog
import xyz.hisname.fireflyiii.ui.piggybank.PiggyRecyclerAdapter
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel

class SearchPiggyDialog: BaseDialog() {

    private val piggyRecyclerAdapter by lazy { PiggyRecyclerAdapter { data: PiggyData -> itemClicked(data) } }
    private val piggyViewModel by lazy { getViewModel(SearchPiggyViewModel::class.java) }
    private lateinit var initialData: PagingData<PiggyData>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.dialog_search, container)
    }

    private fun displayView(){
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recycler_view.adapter = piggyRecyclerAdapter
        piggyViewModel.getAllPiggyBank().observe(viewLifecycleOwner){ pagingData ->
            initialData = pagingData
            piggyRecyclerAdapter.submitData(lifecycle, pagingData)
        }
        piggyRecyclerAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner) { loadStates ->
            swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
        }
    }

    private fun searchData(){
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean{
                piggyViewModel.piggyName.postValue(query)
                dialog?.dismiss()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if(newText.isBlank() or newText.isEmpty()){
                    piggyRecyclerAdapter.submitData(lifecycle, initialData)
                } else {
                    piggyViewModel.searchPiggyBank(newText).observe(viewLifecycleOwner){  pagingData ->
                        piggyRecyclerAdapter.submitData(lifecycle, pagingData)
                    }
                }
                return true
            }

        })
    }



    private fun itemClicked(piggyData: PiggyData){
        piggyViewModel.piggyName.postValue(piggyData.piggyAttributes.name)
        dialog?.dismiss()
    }

    override fun setWidgets() {
        displayView()
        searchData()
    }
}