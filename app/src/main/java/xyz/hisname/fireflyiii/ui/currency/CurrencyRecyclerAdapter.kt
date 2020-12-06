package xyz.hisname.fireflyiii.ui.currency

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.currency_list.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.util.Flags
import xyz.hisname.fireflyiii.util.extension.getCompatColor
import xyz.hisname.fireflyiii.util.extension.inflate

class CurrencyRecyclerAdapter(private val clickListener:(CurrencyData) -> Unit):
        PagingDataAdapter<CurrencyData, CurrencyRecyclerAdapter.CurrencyHolder>(DIFF_CALLBACK){

    private lateinit var context: Context
    private val isThumbnailEnabled by lazy {
        AppPref(PreferenceManager.getDefaultSharedPreferences(context)).isCurrencyThumbnailEnabled
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyHolder {
        context = parent.context
        return CurrencyHolder(parent.inflate(R.layout.currency_list))
    }

    override fun onBindViewHolder(holder: CurrencyHolder, position: Int){
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }

    inner class CurrencyHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(currencyData: CurrencyData, clickListener: (CurrencyData) -> Unit){
            val currency = currencyData.currencyAttributes
            itemView.currencySymbol.text = currency?.symbol.toString()
            itemView.currencyCode.text = currency?.code
            if(currency?.enabled == true){
                itemView.currencyName.text = currency.name + " (" + currency.code + ")"
            } else {
                itemView.currencyName.text = currency?.name + " (" + currency?.code + ")" + " (Disabled)"
                itemView.currencyName.setTextColor(context.getCompatColor(R.color.md_grey_400))
                itemView.currencySymbol.setTextColor(context.getCompatColor(R.color.md_grey_400))
            }
            if(isThumbnailEnabled) {
                itemView.flagImage.isVisible = true
                Glide.with(context)
                        .load(Flags.getFlagByIso(currency?.code ?: ""))
                        .error(R.drawable.unknown)
                        .into(itemView.flagImage)
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