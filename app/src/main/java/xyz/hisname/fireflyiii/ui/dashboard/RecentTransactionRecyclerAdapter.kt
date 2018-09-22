package xyz.hisname.fireflyiii.ui.dashboard

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recent_transaction_list.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.ui.base.DiffUtilAdapter
import xyz.hisname.fireflyiii.util.extension.inflate

class RecentTransactionRecyclerAdapter(private val items: MutableList<TransactionData>):
        DiffUtilAdapter<TransactionData, RecentTransactionRecyclerAdapter.RtAdapter>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RtAdapter {
        context = parent.context
        return RtAdapter(parent.inflate(R.layout.recent_transaction_list))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RtAdapter, position: Int) {
        val transactionData = items[position].attributes
        if(transactionData.description.length >= 25){
            holder.transactionNameText.text = transactionData.description.substring(0,25) + "..."
        } else {
            holder.transactionNameText.text = transactionData.description
        }
        holder.sourceNameText.text = transactionData.source_name
        holder.dateText.text = transactionData.date
        if(transactionData.amount.toString().startsWith("-")){
            // Negative value means it's a withdrawal
            holder.transactionAmountText.setTextColor(ContextCompat.getColor(context, R.color.md_red_500))
            holder.transactionAmountText.text = transactionData.amount.toString()
        } else {
            holder.transactionAmountText.text = transactionData.amount.toString()
        }
    }

    inner class RtAdapter(view: View): RecyclerView.ViewHolder(view) {
        val transactionNameText: TextView = view.transactionNameText
        val sourceNameText: TextView = view.sourceNameText
        val transactionAmountText: TextView = view.transactionAmountText
        val dateText: TextView = view.dateText
    }

}