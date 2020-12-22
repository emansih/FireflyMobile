package xyz.hisname.fireflyiii.ui.transaction.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.dialog_search, container)
    }


    private fun setRecyclerView(){
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recycler_view.adapter = descriptionAdapter
    }

    private fun searchData(){
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean{
                descriptionViewModel.transactionName.postValue(query)
                dialog?.dismiss()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                descriptionViewModel.searchTransactionName(newText).observe(viewLifecycleOwner){ data ->
                    descriptionAdapter.submitData(lifecycle, data)
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
        setRecyclerView()
        searchData()
    }
}