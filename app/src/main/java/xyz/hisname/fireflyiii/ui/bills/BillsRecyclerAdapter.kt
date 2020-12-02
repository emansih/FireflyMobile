package xyz.hisname.fireflyiii.ui.bills

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bills_list_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.util.extension.getCompatColor
import xyz.hisname.fireflyiii.util.extension.inflate

class BillsRecyclerAdapter(private val clickListener:(BillData) -> Unit):
        PagingDataAdapter<BillData, BillsRecyclerAdapter.BillsHolder>(DIFF_CALLBACK) {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillsHolder {
        context = parent.context
        return BillsHolder(parent.inflate(R.layout.bills_list_item))
    }

    override fun onBindViewHolder(holder: BillsHolder, position: Int) {
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }

    inner class BillsHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(billData: BillData, clickListener: (BillData) -> Unit) = with(itemView) {
            val billResponse = billData.billAttributes
            var billName = billResponse?.name
            val isPending = billResponse?.isPending
            if(billName != null){
                if(isPending == true){
                    billName = "$billName (Pending)"
                    itemView.billName.setTextColor(context.getCompatColor(R.color.md_red_500))
                }
                itemView.billName.text = billName
            }
            itemView.setOnClickListener{clickListener(billData)}
            itemView.billAmount.text = context.getString(R.string.bill_amount, billResponse?.currency_symbol,
                    billResponse?.amount_max)
            val freq = billResponse?.repeat_freq
            if(freq != null && freq.isNotBlank()){
                itemView.billFreq.text = freq.substring(0,1).toUpperCase() + freq.substring(1)
            }
            itemView.billId.text = billData.billId.toString()
            itemView.billCard.setOnClickListener{clickListener(billData)}
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<BillData>() {
            override fun areItemsTheSame(oldBill: BillData,
                                         newBill: BillData) = oldBill == newBill

            override fun areContentsTheSame(oldBill: BillData,
                                            newBill: BillData) = oldBill == newBill
        }
    }
}
