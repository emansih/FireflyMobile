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

package xyz.hisname.fireflyiii.ui.bills

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xyz.hisname.fireflyiii.databinding.BillsDialogBinding
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel
import xyz.hisname.fireflyiii.util.extension.toastInfo

class BillsBottomSheet: BottomSheetDialogFragment() {

    private var billsDialogBinding: BillsDialogBinding? = null
    private val binding get() = billsDialogBinding!!
    private val billsBottomSheetViewModel by lazy { getImprovedViewModel(BillsBottomSheetViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        billsDialogBinding = BillsDialogBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.billDialogRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.billDialogRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL))
        billsBottomSheetViewModel.billPayableToday.observe(viewLifecycleOwner){ value ->
            binding.amountDueToday.text = value
        }
        billsBottomSheetViewModel.getBills().observe(viewLifecycleOwner){ billStatus ->
            if(billStatus.isEmpty()){
                toastInfo("No payable bills today!")
                dismiss()
            } else {
                val itemAdapter = BillsStatusRecyclerAdapter(billStatus)
                binding.billDialogRecyclerView.adapter = itemAdapter
            }

        }
    }

}