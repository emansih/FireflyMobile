package xyz.hisname.fireflyiii.ui.currency

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.currency_list.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.ui.base.DiffUtilAdapter
import xyz.hisname.fireflyiii.util.Flags
import xyz.hisname.fireflyiii.util.extension.inflate

class CurrencyRecyclerAdapter(private val items: MutableList<CurrencyData>, private val clickListener:(CurrencyData) -> Unit):
DiffUtilAdapter<CurrencyData, CurrencyRecyclerAdapter.CurrencyHolder>(){

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyHolder {
        context = parent.context
        return CurrencyHolder(parent.inflate(R.layout.currency_list))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: CurrencyHolder, position: Int) = holder.bind(items[position], clickListener)


    inner class CurrencyHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(currencyData: CurrencyData, clickListener: (CurrencyData) -> Unit){
            val currency = currencyData.currencyAttributes
            itemView.currencyName.text = currency?.name + " (" + currency?.code + ")"
            itemView.currencySymbol.text = currency?.symbol.toString()
            itemView.flagImage.setImageDrawable(Flags(context).getCurrencyFlagsByIso(currency?.code!!))
            itemView.setOnClickListener {
                clickListener(currencyData)
            }
        }

    }
}