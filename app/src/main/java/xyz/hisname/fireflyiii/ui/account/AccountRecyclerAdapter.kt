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

package xyz.hisname.fireflyiii.ui.account

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.AccountListItemBinding
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.util.extension.getCompatColor
import java.math.BigDecimal

class AccountRecyclerAdapter(private val clickListener:(AccountData) -> Unit):
        PagingDataAdapter<AccountData, AccountRecyclerAdapter.AccountViewHolder>(DIFF_CALLBACK){

    private lateinit var context: Context
    private var accountListItemBinding: AccountListItemBinding? = null
    private val binding get() = accountListItemBinding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        context = parent.context
        accountListItemBinding = AccountListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int){
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }


    inner class AccountViewHolder(itemView: AccountListItemBinding): RecyclerView.ViewHolder(itemView.root) {
        fun bind(data: AccountData, clickListener: (AccountData) -> Unit){
            val accountData = data.accountAttributes
            var currencySymbol = ""
            if(!accountData.active){
                binding.accountNameText.setTextColor(context.getCompatColor(R.color.material_grey_600))
                binding.accountNumberText.setTextColor(context.getCompatColor(R.color.material_grey_600))
            }
            if(accountData.currency_symbol != null){
                currencySymbol = accountData.currency_symbol
            }
            if(accountData.account_number != null){
                binding.accountNumberText.text = accountData.account_number
            } else {
                binding.accountNumberText.isVisible = false
            }
            val isPending = data.accountAttributes.isPending
            if(isPending){
                binding.accountNameText.text = accountData.name + " (Pending)"
                binding.accountNameText.setTextColor(context.getCompatColor(R.color.md_red_500))
            } else {
                binding.accountNameText.text = accountData.name
            }
            val amount = accountData.current_balance
            if(amount < BigDecimal.ZERO){
                binding.accountAmountText.setTextColor(context.getCompatColor(R.color.md_red_500))
            } else if(amount > BigDecimal.ZERO){
                binding.accountAmountText.setTextColor(context.getCompatColor(R.color.md_green_500))
            }
            binding.accountAmountText.text = currencySymbol + " " + amount
            binding.accountId.text = data.accountId.toString()
            itemView.setOnClickListener { clickListener(data) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<AccountData>() {
            override fun areItemsTheSame(oldAccountData: AccountData,
                                         newAccountData: AccountData) =
                    oldAccountData == newAccountData

            override fun areContentsTheSame(oldAccountData: AccountData,
                                            newAccountData: AccountData) = oldAccountData == newAccountData
        }
    }
}