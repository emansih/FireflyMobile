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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xyz.hisname.fireflyiii.databinding.CurrencyBottomSheetBinding
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.hideKeyboard

class CurrencyListBottomSheet: BottomSheetDialogFragment() {

    private val currencyViewModel by lazy { getViewModel(CurrencyBottomSheetViewModel::class.java) }
    private val currencyAdapter by lazy { CurrencyRecyclerAdapter(false) { data: CurrencyData -> itemClicked(data)}}
    private var currencyBottomSheetBinding: CurrencyBottomSheetBinding? = null
    private val binding get() = currencyBottomSheetBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currencyBottomSheetBinding = CurrencyBottomSheetBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboard()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        binding.recyclerView.adapter = currencyAdapter
        currencyViewModel.getCurrencyList().observe(viewLifecycleOwner){ pagingData ->
            currencyAdapter.submitData(lifecycle, pagingData)
        }
    }

    private fun itemClicked(currencyData: CurrencyData){
        currencyViewModel.currencyCode.postValue(currencyData.currencyAttributes.code)
        currencyViewModel.currencyFullDetails.postValue(currencyData.currencyAttributes.name + " (" + currencyData.currencyAttributes.code + ")")
        dismiss()
    }
}