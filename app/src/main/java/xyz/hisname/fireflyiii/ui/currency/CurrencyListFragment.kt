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

package xyz.hisname.fireflyiii.ui.currency

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.currency_list.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*

class CurrencyListFragment: BaseFragment() {

    private val currencyAdapter by lazy { CurrencyRecyclerAdapter { data: CurrencyData -> clickListener(data) } }
    private val currencyViewModel by lazy { getImprovedViewModel(CurrencyListViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.base_swipe_layout, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboard()
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recycler_view.adapter = currencyAdapter
        initFab()
        refreshData()
        displayView()
        enableDragDrop()
    }

    private fun enableDragDrop(){
        recycler_view.enableDragDrop(extendedFab) { viewHolder, isCurrentlyActive ->
            if (viewHolder.itemView.currencyList.isOverlapping(extendedFab)) {
                extendedFab.dropToRemove()
                if (!isCurrentlyActive) {
                    val currencyName = viewHolder.itemView.currencyName.text
                    currencyViewModel.deleteCurrency(viewHolder.itemView.currencyCode.text.toString()).observe(viewLifecycleOwner) { isDeleted ->
                        currencyAdapter.refresh()
                        if (isDeleted) {
                            toastSuccess(resources.getString(R.string.currency_deleted, currencyName))
                        } else {
                            toastOffline(resources.getString(R.string.data_will_be_deleted_later, currencyName), Toast.LENGTH_LONG)
                        }
                    }
                }
            }
        }
    }

    private fun displayView(){
        currencyAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner){ loadStates ->
            swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
        }
        currencyViewModel.getCurrencyList().observe(viewLifecycleOwner){ pagingData ->
            currencyAdapter.submitData(lifecycle, pagingData)
        }
    }

    private fun clickListener(data: CurrencyData){
        parentFragmentManager.commit {
            replace(R.id.bigger_fragment_container, AddCurrencyFragment().apply {
                arguments = bundleOf("currencyId" to data.currencyId)
            })
            addToBackStack(null)
        }
    }

    private fun initFab(){
        extendedFab.display {
            extendedFab.isClickable = false
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, AddCurrencyFragment().apply {
                    arguments = bundleOf("revealX" to extendedFab.width / 2, "revealY" to extendedFab.height / 2)
                })
                addToBackStack(null)
            }
            extendedFab.isClickable = true
        }
    }

    private fun refreshData(){
        swipeContainer.setOnRefreshListener {
           currencyAdapter.refresh()
        }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.currency)
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = resources.getString(R.string.currency)
    }

}