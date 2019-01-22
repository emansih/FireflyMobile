package xyz.hisname.fireflyiii.ui.bills

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bills_list_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.ui.base.DiffUtilAdapter
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.inflate
import java.text.DecimalFormat

class BillsRecyclerAdapter(private val items: MutableList<BillData>, private val clickListener:(BillData) -> Unit):
        DiffUtilAdapter<BillData,BillsRecyclerAdapter.BillsHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillsHolder {
        context = parent.context
        return BillsHolder(parent.inflate(R.layout.bills_list_item))
    }

    override fun onBindViewHolder(holder: BillsHolder, position: Int) {
        holder.bind(items[position], clickListener)
    }

    override fun getItemCount() = items.size

    inner class BillsHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(billData: BillData, clickListener: (BillData) -> Unit) = with(itemView) {
            val billResponse = billData.billAttributes
            /*val date = billResponse?.date
            val daysDiff = DateTimeUtil.getDaysDifference(date).toInt()
            itemView.billDue.let {
                when {
                    daysDiff == 0 -> it.text = context.getString(R.string.bill_due_today)
                    daysDiff == 1 -> it.text = context.getString(R.string.bill_due_tomorrow)
                    daysDiff < 0 -> it.text = context.getString(R.string.bill_due_past, Math.abs(daysDiff))
                    daysDiff == -1 -> it.text = context.getString(R.string.bill_due_yesterday)
                    daysDiff >= 0 -> {
                        itemView.billDue.setTextColor(ContextCompat.getColor(context, R.color.md_black_1000))
                        it.text = context.getString(R.string.bill_due_future,
                                DateTimeUtil.getDayOfWeek(date!!),date)
                    }
                }
            }*/
            val billName = billResponse?.name
            if(billName != null){
                if(billName.length >= 17){
                    itemView.billName.text = billName.substring(0,17) + "..."
                } else {
                    itemView.billName.text = billName
                }
            }
            itemView.setOnClickListener{clickListener(billData)}
            itemView.billAmount.text = context.getString(R.string.bill_amount, billResponse?.currency_code,
                    DecimalFormat("0.00").format(billResponse?.amount_max).toString())
            val freq = billResponse?.repeat_freq
            if(freq!!.isNotBlank() or freq.isNotEmpty()){
                @SuppressLint("SetTextI18n")
                itemView.billFreq.text = freq.substring(0,1).toUpperCase() + freq.substring(1)

            }
            itemView.billCard.setOnClickListener{clickListener(billData)}
        }
    }
}
