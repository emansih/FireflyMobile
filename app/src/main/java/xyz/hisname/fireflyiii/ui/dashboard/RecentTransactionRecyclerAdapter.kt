package xyz.hisname.fireflyiii.ui.dashboard

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recent_transaction_list.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.ui.base.DiffUtilAdapter
import xyz.hisname.fireflyiii.util.extension.inflate

class RecentTransactionRecyclerAdapter(private val items: MutableList<TransactionData>):
        DiffUtilAdapter<TransactionData, RecentTransactionRecyclerAdapter.RtAdapter>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            RtAdapter(parent.inflate(R.layout.recent_transaction_list))

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
        holder.transactionAmountText.text = transactionData.amount.toString()
    }

    inner class RtAdapter(view: View): RecyclerView.ViewHolder(view) {
        val transactionNameText: TextView = view.transactionNameText
        val sourceNameText: TextView = view.sourceNameText
        val transactionAmountText: TextView = view.transactionAmountText
        val dateText: TextView = view.dateText
    }

}