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
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.databinding.CurrencyListBinding
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.util.Flags
import xyz.hisname.fireflyiii.util.extension.getCompatColor
import xyz.hisname.fireflyiii.util.getUniqueHash

class CurrencyRecyclerAdapter(private val shouldShowDisabled: Boolean = true,
                              private val clickListener:(CurrencyData) -> Unit):
        PagingDataAdapter<CurrencyData, CurrencyRecyclerAdapter.CurrencyHolder>(DIFF_CALLBACK){

    private lateinit var context: Context
    private val isThumbnailEnabled by lazy {
        val uniqueHash: String
        runBlocking(Dispatchers.IO) {
            uniqueHash = context.getUniqueHash()
        }
        AppPref(context.getSharedPreferences("$uniqueHash-user-preferences", Context.MODE_PRIVATE)).isCurrencyThumbnailEnabled
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyHolder {
        context = parent.context
        val itemView = CurrencyListBinding.inflate(LayoutInflater.from(context), parent, false)
        return CurrencyHolder(itemView)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: CurrencyHolder, position: Int){
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }

    inner class CurrencyHolder(
        private val currencyView: CurrencyListBinding
    ): RecyclerView.ViewHolder(currencyView.root){
        fun bind(currencyData: CurrencyData, clickListener: (CurrencyData) -> Unit){
            val currency = currencyData.currencyAttributes
            currencyView.currencySymbol.text = currency.symbol
            currencyView.currencyCode.text = currency.code
            if(shouldShowDisabled){
                if(!currency.enabled){
                    currencyView.currencyName.text = currency.name + " (" + currency.code + ")" + " (Disabled)"
                    currencyView.currencyName.setTextColor(context.getCompatColor(R.color.md_grey_400))
                    currencyView.currencySymbol.setTextColor(context.getCompatColor(R.color.md_grey_400))
                }
            }
            currencyView.currencyName.text = currency.name + " (" + currency.code + ")"
            if(isThumbnailEnabled) {
                currencyView.flagImage.isVisible = true
                Glide.with(context)
                        .load(Flags.getFlagByIso(currency.code))
                        .error(R.drawable.unknown)
                        .into(currencyView.flagImage)
            }
            itemView.setOnClickListener {
                clickListener(currencyData)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<CurrencyData>() {
            override fun areItemsTheSame(oldCurrency: CurrencyData,
                                         newCurrency: CurrencyData) =
                    oldCurrency == newCurrency

            override fun areContentsTheSame(oldCurrency: CurrencyData,
                                            newCurrency: CurrencyData) = oldCurrency == newCurrency
        }
    }
}