package xyz.hisname.fireflyiii.ui.currency

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
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
            val requestOptions =
                    RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(IconicsDrawable(context)
                                    .icon(GoogleMaterial.Icon.gmd_file_download)
                                    .sizeDp(24))
                            .error(IconicsDrawable(context)
                                    .icon(GoogleMaterial.Icon.gmd_error)
                                    .sizeDp(24))
            Glide.with(context)
                    .load(Flags.getFlagByIso(currency?.code!!))
                    .apply(requestOptions)
                    .into(itemView.flagImage)
            itemView.setOnClickListener {
                clickListener(currencyData)
            }
        }

    }
}