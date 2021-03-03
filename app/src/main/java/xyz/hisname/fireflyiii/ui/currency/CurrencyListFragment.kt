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
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.BaseSwipeLayoutBinding
import xyz.hisname.fireflyiii.databinding.CurrencyListBinding
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*

class CurrencyListFragment: BaseFragment() {

    private val currencyAdapter by lazy { CurrencyRecyclerAdapter { data: CurrencyData -> clickListener(data) } }
    private val currencyViewModel by lazy { getImprovedViewModel(CurrencyListViewModel::class.java) }
    private var baseSwipeLayoutBinding: BaseSwipeLayoutBinding? = null
    private val binding get() = baseSwipeLayoutBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        baseSwipeLayoutBinding = BaseSwipeLayoutBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboard()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.recyclerView.adapter = currencyAdapter
        initFab()
        refreshData()
        displayView()
        enableDragDrop()
    }

    private fun enableDragDrop(){
        binding.recyclerView.enableDragDrop(extendedFab) { viewHolder, isCurrentlyActive ->
            val currencyListBinding = CurrencyListBinding.bind(viewHolder.itemView)
            if (currencyListBinding.currencyList.isOverlapping(extendedFab)) {
                extendedFab.dropToRemove()
                if (!isCurrentlyActive) {
                    val currencyName = currencyListBinding.currencyName.text
                    currencyViewModel.deleteCurrency(currencyListBinding.currencyCode.text.toString()).observe(viewLifecycleOwner) { isDeleted ->
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
            binding.swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
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
        binding.swipeContainer.setOnRefreshListener {
           currencyAdapter.refresh()
        }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = resources.getString(R.string.currency)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = resources.getString(R.string.currency)
    }

}