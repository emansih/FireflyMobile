package xyz.hisname.fireflyiii.ui.piggybank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.dialog_search.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.piggybank.PiggyViewModel
import xyz.hisname.fireflyiii.ui.base.BaseDialog
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel

class PiggyDialog: BaseDialog() {

    private var dataAdapter = arrayListOf<PiggyData>()
    private var initialAdapter = arrayListOf<PiggyData>()
    private lateinit var piggyRecyclerAdapter: PiggyRecyclerAdapter
    private val piggyViewModel by lazy { getViewModel(PiggyViewModel::class.java) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.dialog_search, container)
    }

    private fun displayView(){
        recycler_view.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter?.notifyDataSetChanged()
        }
        swipeContainer.isRefreshing = true
        piggyViewModel.getAllPiggyBanks().observe(viewLifecycleOwner) { piggyBankData ->
            swipeContainer.isRefreshing = false
            initialAdapter.addAll(piggyBankData)
            recycler_view.adapter = PiggyRecyclerAdapter(piggyBankData) { data: PiggyData -> itemClicked(data) }
        }
    }

    private fun searchData(){
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean{
                piggyViewModel.postPiggyName(query)
                dialog?.dismiss()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if(newText.isBlank() or newText.isEmpty()){
                    piggyRecyclerAdapter = PiggyRecyclerAdapter(initialAdapter) { data: PiggyData -> itemClicked(data) }
                    piggyRecyclerAdapter.notifyDataSetChanged()
                    recycler_view.adapter = piggyRecyclerAdapter
                } else {
                    filter(newText)
                }
                return true
            }

        })
    }

    private fun filter(piggyName: String){
        dataAdapter.clear()
        piggyViewModel.getPiggyByName(piggyName).observe(viewLifecycleOwner) { piggyData ->
            piggyData.forEachIndexed { _, catData ->
                dataAdapter.add(catData)
            }
            piggyRecyclerAdapter = PiggyRecyclerAdapter(dataAdapter) { data: PiggyData ->  itemClicked(data)}
            piggyRecyclerAdapter.notifyDataSetChanged()
            recycler_view.adapter = piggyRecyclerAdapter
        }
    }


    private fun itemClicked(piggyData: PiggyData){
        piggyViewModel.postPiggyName(piggyData.piggyAttributes?.name)
        dialog?.dismiss()
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