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
        PagingDataAdapter<AccountData, AccountRecyclerAdapter.AccountViewHolder>(DIFF_CALLBACK) {
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        context = parent.context
        val itemView = AccountListItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return AccountViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int){
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }

    inner class AccountViewHolder(
        private val accountView: AccountListItemBinding
    ): RecyclerView.ViewHolder(accountView.root) {
        fun bind(data: AccountData, clickListener: (AccountData) -> Unit){
            val accountData = data.accountAttributes
            var currencySymbol = ""
            if(!accountData.active){
                accountView.accountNameText.setTextColor(context.getCompatColor(R.color.material_grey_600))
                accountView.accountNumberText.setTextColor(context.getCompatColor(R.color.material_grey_600))
            }
            if(accountData.currency_symbol != null){
                currencySymbol = accountData.currency_symbol
            }
            if(accountData.account_number != null){
                accountView.accountNumberText.text = accountData.account_number
            } else {
                accountView.accountNumberText.isVisible = false
            }
            val isPending = data.accountAttributes.isPending
            if(isPending){
                accountView.accountNameText.text = accountData.name + " (Pending)"
                accountView.accountNameText.setTextColor(context.getCompatColor(R.color.md_red_500))
            } else {
                accountView.accountNameText.text = accountData.name
            }
            val amount = accountData.current_balance
            if(amount < BigDecimal.ZERO){
                accountView.accountAmountText.setTextColor(context.getCompatColor(R.color.md_red_500))
            } else if(amount > BigDecimal.ZERO){
                accountView.accountAmountText.setTextColor(context.getCompatColor(R.color.md_green_500))
            }
            accountView.accountAmountText.text = currencySymbol + " " + amount
            accountView.accountId.text = data.accountId.toString()
            itemView.setOnClickListener { clickListener(data) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<AccountData>() {
            override fun areItemsTheSame(oldAccountData: AccountData,
                                         newAccountData: AccountData) =
                    oldAccountData.accountId == newAccountData.accountId

            override fun areContentsTheSame(oldAccountData: AccountData,
                                            newAccountData: AccountData) = oldAccountData == newAccountData
        }
    }
}