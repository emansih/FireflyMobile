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

package xyz.hisname.fireflyiii.ui.account.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.AccountListItemBinding
import xyz.hisname.fireflyiii.databinding.FragmentBaseListBinding
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.ui.account.AccountRecyclerAdapter
import xyz.hisname.fireflyiii.ui.account.AddAccountFragment
import xyz.hisname.fireflyiii.ui.account.details.AccountDetailFragment
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*

class ListAccountFragment: BaseFragment() {

    private val accountType by lazy { arguments?.getString("accountType") ?: "" }
    private val accountVm by lazy { getImprovedViewModel(ListAccountViewModel::class.java) }
    private val accountAdapter by lazy { AccountRecyclerAdapter { data: AccountData -> itemClicked(data) } }

    private var fragmentBaseListBinding: FragmentBaseListBinding? = null
    private val binding get() = fragmentBaseListBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentBaseListBinding = FragmentBaseListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        displayView()
        pullToRefresh()
        initFab()
        enableDragAndDrop()
    }

    private fun enableDragAndDrop(){
        binding.baseSwipeLayout.recyclerView.enableDragDrop(extendedFab) { viewHolder, isCurrentlyActive ->
            val accountListBinding = AccountListItemBinding.bind(viewHolder.itemView)
            if (accountListBinding.accountList.isOverlapping(extendedFab)){
                extendedFab.dropToRemove()
                if(!isCurrentlyActive){
                    val accountName = accountListBinding.accountNameText.text.toString()
                    val accountId = accountListBinding.accountId.text.toString()
                    accountVm.deleteAccountByName(accountId).observe(viewLifecycleOwner){ isDeleted ->
                        accountAdapter.refresh()
                        if(isDeleted){
                            when (accountType){
                                "asset" -> toastSuccess(resources.getString(R.string.asset_account_deleted, accountName))
                                "expense" -> toastSuccess(resources.getString(R.string.expense_account_deleted, accountName))
                                "revenue" -> toastSuccess(resources.getString(R.string.revenue_account_deleted, accountName))
                                "liability" -> toastSuccess(resources.getString(R.string.liability_account_deleted, accountName))
                            }
                        } else {
                            toastOffline(resources.getString(R.string.data_will_be_deleted_later, accountName), Toast.LENGTH_LONG)
                        }
                    }
                }
            }
        }
    }

    private fun displayView(){
        accountVm.getAccountList(accountType).observe(viewLifecycleOwner){ pagingData ->
            accountAdapter.submitData(lifecycle, pagingData)
        }
    }


    private fun setRecyclerView(){
        binding.baseSwipeLayout.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.baseSwipeLayout.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.baseSwipeLayout.recyclerView.adapter = accountAdapter
        accountAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner){ loadStates ->
            binding.baseSwipeLayout.swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
            if(loadStates.refresh !is LoadState.Loading) {
                if(accountAdapter.itemCount < 1){
                    binding.baseSwipeLayout.recyclerView.isGone = true
                    binding.listImage.isVisible = true
                    binding.listText.isVisible = true
                    when (accountType) {
                        "asset" -> {
                            binding.listImage.setImageDrawable(IconicsDrawable(requireContext(), FontAwesome.Icon.faw_money_bill))
                            binding.listText.text = resources.getString(R.string.no_account_found, resources.getString(R.string.asset_account))
                        }
                        "expense" -> {
                            binding.listImage.setImageDrawable(IconicsDrawable(requireContext(), FontAwesome.Icon.faw_shopping_cart))
                            binding.listText.text = resources.getString(R.string.no_account_found, resources.getString(R.string.expense_account))
                        }
                        "revenue" -> {
                            binding.listImage.setImageDrawable(IconicsDrawable(requireContext(), FontAwesome.Icon.faw_download))
                            binding.listText.text = resources.getString(R.string.no_account_found, resources.getString(R.string.revenue_account))
                        }
                        "liabilities" -> {
                            binding.listImage.setImageDrawable(IconicsDrawable(requireContext(), FontAwesome.Icon.faw_ticket_alt))
                            binding.listText.text = resources.getString(R.string.no_account_found, resources.getString(R.string.liability_account))
                        }
                    }
                } else {
                    binding.baseSwipeLayout.recyclerView.isVisible = true
                    binding.listImage.isGone = true
                    binding.listText.isGone = true
                }
            }
        }
    }

    private fun itemClicked(data: AccountData){
        val bundle = bundleOf("accountId" to data.accountId, "accountType" to accountType)
        parentFragmentManager.commit {
            addToBackStack(null)
            replace(R.id.fragment_container, AccountDetailFragment().apply { arguments = bundle })
        }
    }

    private fun pullToRefresh(){
        binding.baseSwipeLayout.swipeContainer.setOnRefreshListener {
            displayView()
        }
    }

    private fun initFab(){
        extendedFab.display {
            extendedFab.isClickable = false
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, AddAccountFragment().apply{
                    arguments = bundleOf("revealX" to extendedFab.width / 2, "revealY" to extendedFab.height / 2, "accountType" to accountType)
                })
                addToBackStack(null)
            }
            extendedFab.isClickable = true
        }
    }

    private fun convertString(): String{
        return when {
            Objects.equals(accountType, "asset") -> resources.getString(R.string.asset_account)
            Objects.equals(accountType, "expense") -> resources.getString(R.string.expense_account)
            Objects.equals(accountType, "revenue") -> resources.getString(R.string.revenue_account)
            Objects.equals(accountType, "liabilities") -> resources.getString(R.string.liability_account)
            else -> "Accounts"
        }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = convertString()
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = convertString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentBaseListBinding = null
    }


}