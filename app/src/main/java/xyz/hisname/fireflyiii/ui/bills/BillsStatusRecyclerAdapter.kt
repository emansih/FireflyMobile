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

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.IconicsColor
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.color
import com.mikepenz.iconics.utils.sizeDp
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.BillDialogRecyclerViewBinding
import xyz.hisname.fireflyiii.repository.models.bills.BillsStatusModel

class BillsStatusRecyclerAdapter(private val billStatus: List<BillsStatusModel>): RecyclerView.Adapter<BillsStatusRecyclerAdapter.BillsStatusHolder>() {

    private var billDialogRecyclerBinding: BillDialogRecyclerViewBinding? = null
    private val binding get() = billDialogRecyclerBinding!!
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillsStatusHolder {
        context = parent.context
        billDialogRecyclerBinding = BillDialogRecyclerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillsStatusHolder(binding)
    }

    override fun onBindViewHolder(holder: BillsStatusHolder, position: Int) {
        holder.bind(billStatus[position])
    }

    override fun getItemCount() = billStatus.size

    inner class BillsStatusHolder(itemView: BillDialogRecyclerViewBinding): RecyclerView.ViewHolder(itemView.root) {
        fun bind(billStatusModel: BillsStatusModel) {
            binding.billName.text = billStatusModel.billName
            binding.billAmount.text = context.getString(R.string.bill_amount,
                    billStatusModel.billCurrency, billStatusModel.billAmount)
            if(billStatusModel.isBillPaid){
                binding.billStatusImage.setImageDrawable(IconicsDrawable(context).apply {
                    icon = GoogleMaterial.Icon.gmd_check
                    color = IconicsColor.colorInt(context.getColor(R.color.md_green_500))
                    sizeDp = 24
                })
            } else {
                binding.billStatusImage.setImageDrawable(IconicsDrawable(context).apply {
                    icon = GoogleMaterial.Icon.gmd_add_alert
                    color = IconicsColor.colorInt(context.getColor(R.color.md_red_500))
                    sizeDp = 24
                })
            }
            binding.billId.text = billStatusModel.billId.toString()
        }
    }
}