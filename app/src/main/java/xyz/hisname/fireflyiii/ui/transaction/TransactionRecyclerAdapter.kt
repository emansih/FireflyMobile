package xyz.hisname.fireflyiii.ui.transaction

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recent_transaction_list.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.base.DiffUtilAdapter
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.getCompatColor
import xyz.hisname.fireflyiii.util.extension.inflate

class TransactionRecyclerAdapter(private val items: MutableList<Transactions>, private val clickListener:(Transactions) -> Unit):
        DiffUtilAdapter<Transactions, TransactionRecyclerAdapter.RtAdapter>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RtAdapter {
        context = parent.context
        return RtAdapter(parent.inflate(R.layout.recent_transaction_list))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RtAdapter, position: Int)  = holder.bind(items[position], clickListener)

    inner class RtAdapter(view: View): RecyclerView.ViewHolder(view) {
        fun bind(transactionAttributes: Transactions, clickListener: (Transactions) -> Unit){
            val timePreference = AppPref(PreferenceManager.getDefaultSharedPreferences(context)).timeFormat
            val transactionDescription = transactionAttributes.description
            if(transactionDescription.length >= 25){
                if(transactionAttributes.isPending){
                    itemView.transactionNameText.setTextColor(context.getCompatColor(R.color.md_red_500))
                    itemView.transactionNameText.text = transactionDescription.substring(0,25) + "..." + " (Pending)"
                } else {
                    itemView.transactionNameText.text = transactionDescription.substring(0,25) + "..."
                }
            } else {
                if(transactionAttributes.isPending) {
                    itemView.transactionNameText.setTextColor(context.getCompatColor(R.color.md_red_500))
                    itemView.transactionNameText.text = transactionDescription + " (Pending)"
                } else {
                    itemView.transactionNameText.text = transactionDescription
                }
            }
            itemView.sourceNameText.text = transactionAttributes.source_name
            itemView.dateText.text = DateTimeUtil.convertLocalDateTime(transactionAttributes.date , timePreference)
            if(transactionAttributes.amount.toString().startsWith("-")){
               // Negative value means it's a withdrawal
               itemView.transactionAmountText.setTextColor(context.getCompatColor(R.color.md_red_500))
               itemView.transactionAmountText.text = "-" + transactionAttributes.currency_symbol +
                       Math.abs(transactionAttributes.amount)
            } else {
               itemView.transactionAmountText.text = transactionAttributes.currency_symbol +
                       transactionAttributes.amount.toString()
            }
            itemView.list_item.setOnClickListener {clickListener(transactionAttributes)}
        }
    }

}