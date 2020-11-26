package xyz.hisname.fireflyiii.ui.transaction

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.transaction_card_details.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionAmountMonth
import xyz.hisname.fireflyiii.ui.base.DiffUtilAdapter
import xyz.hisname.fireflyiii.util.extension.inflate

class TransactionMonthRecyclerView(private val items: List<TransactionAmountMonth>,
                                   private val clickListener:(Int) -> Unit):
        DiffUtilAdapter<TransactionAmountMonth, TransactionMonthRecyclerView.TransactionAdapter>() {

    private lateinit var context: Context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionAdapter {
        context = parent.context
        return TransactionAdapter(parent.inflate(R.layout.transaction_card_details))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: TransactionAdapter, position: Int) = holder.bind(items[position], position)


    inner class TransactionAdapter(view: View): RecyclerView.ViewHolder(view) {
        fun bind(transactionData: TransactionAmountMonth, click: Int) {
            itemView.transaction_freq.text = transactionData.transactionFreq.toString()
            itemView.spentAmount.text = transactionData.transactionAmount
            itemView.transaction_card_date.text = transactionData.monthYear
            itemView.setOnClickListener { clickListener(click) }
        }
    }

}