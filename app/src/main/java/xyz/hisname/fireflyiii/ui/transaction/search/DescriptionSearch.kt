/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import xyz.hisname.fireflyiii.databinding.BaseSwipeLayoutBinding
import xyz.hisname.fireflyiii.databinding.DialogSearchBinding
import xyz.hisname.fireflyiii.ui.base.BaseDialog
import xyz.hisname.fireflyiii.util.extension.getViewModel

class DescriptionSearch: BaseDialog() {

    private val descriptionAdapter by lazy { DescriptionAdapter { data: String -> itemClicked(data) } }
    private val descriptionViewModel by lazy { getViewModel(DescriptionViewModel::class.java) }
    private lateinit var initialData: PagingData<String>
    private var dialogSearchBinding: DialogSearchBinding? = null
    private val binding get() = dialogSearchBinding!!
    private var baseSwipeBinding: BaseSwipeLayoutBinding? = null
    private val baseBinding get() = baseSwipeBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialogSearchBinding = DialogSearchBinding.inflate(inflater, container, false)
        baseSwipeBinding = binding.dialogSearchSwipeLayout
        val view = binding.root
        return view
    }


    private fun displayView(){
        baseBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        baseBinding.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        baseBinding.recyclerView.adapter = descriptionAdapter
        descriptionViewModel.searchTransactionName("").observe(viewLifecycleOwner){ data ->
            initialData = data
            descriptionAdapter.submitData(lifecycle, data)
        }
        descriptionAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner) { loadStates ->
            baseBinding.swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
        }
    }

    private fun searchData(){
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

    override fun onDestroyView() {
        super.onDestroyView()
        dialogSearchBinding = null
        baseSwipeBinding = null
    }
}