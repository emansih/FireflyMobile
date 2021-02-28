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

package xyz.hisname.fireflyiii.ui.account.search

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
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.ui.account.AccountRecyclerAdapter
import xyz.hisname.fireflyiii.ui.base.BaseDialog
import xyz.hisname.fireflyiii.util.extension.getViewModel

class AccountSearchDialog: BaseDialog() {

    private val accountType by lazy { arguments?.getString("accountType") ?: "" }
    private val accountSearchViewModel by lazy { getViewModel(AccountSearchViewModel::class.java) }
    private val accountAdapter by lazy { AccountRecyclerAdapter { data: AccountData -> itemClicked(data)} }
    private lateinit var initialData: PagingData<AccountData>
    private var dialogSearchBinding: DialogSearchBinding? = null
    private val binding get() = dialogSearchBinding!!
    private var baseSwipe: BaseSwipeLayoutBinding? = null
    private val baseSwipeBinding get() = baseSwipe!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialogSearchBinding = DialogSearchBinding.inflate(inflater, container, false)
        baseSwipe = binding.dialogSearchSwipeLayout
        val view = binding.root
        return view
    }

    private fun displayView(){
        baseSwipeBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        baseSwipeBinding.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        baseSwipeBinding.recyclerView.adapter = accountAdapter
        accountSearchViewModel.getAccountList(accountType).observe(viewLifecycleOwner){ pagingData ->
            initialData = pagingData
            accountAdapter.submitData(lifecycle, pagingData)
        }
        accountAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner) { loadStates ->
            baseSwipeBinding.swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
        }
    }

    private fun searchData(){
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean{
                accountSearchViewModel.accountName.postValue(query)
                dialog?.dismiss()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if(newText.isBlank() or newText.isEmpty()){
                    accountAdapter.submitData(lifecycle, initialData)
                } else {
                    accountSearchViewModel.searchAccount(newText, accountType).observe(viewLifecycleOwner){ pagingData ->
                        accountAdapter.submitData(lifecycle, pagingData)
                    }
                }
                return true
            }

        })
    }



    private fun itemClicked(accountData: AccountData){
        accountSearchViewModel.accountName.postValue(accountData.accountAttributes.name)
        dialog?.dismiss()
    }


    override fun setWidgets() {
        displayView()
        searchData()
    }
}