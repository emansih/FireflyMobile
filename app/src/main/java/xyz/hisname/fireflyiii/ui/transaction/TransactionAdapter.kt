package xyz.hisname.fireflyiii.ui.transaction

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recent_transaction_list.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.getCompatColor
import xyz.hisname.fireflyiii.util.extension.inflate

class TransactionAdapter(private val clickListener:(Transactions) -> Unit):
        PagingDataAdapter<Transactions, TransactionAdapter.TransactionViewHolder>(DIFF_CALLBACK) {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        context = parent.context
        return TransactionViewHolder(parent.inflate(R.layout.recent_transaction_list))
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int){
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }

    inner class TransactionViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(transactionAttributes: Transactions, clickListener: (Transactions) -> Unit){
            val timePreference = AppPref(PreferenceManager.getDefaultSharedPreferences(context)).timeFormat
            val transactionDescription = transactionAttributes.description
            val descriptionText = if(transactionAttributes.isPending){
                itemView.transactionNameText.setTextColor(context.getCompatColor(R.color.md_red_500))
                "$transactionDescription (Pending)"
            } else {
                transactionDescription
            }
            itemView.transactionNameText.text = descriptionText
            itemView.sourceNameText.text = transactionAttributes.source_name
            itemView.transactionJournalId.text = transactionAttributes.transaction_journal_id.toString()
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

    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<Transactions>() {
            override fun areItemsTheSame(oldTransactions: Transactions,
                                         newTransactions: Transactions) =
                    oldTransactions == newTransactions

            override fun areContentsTheSame(oldTransactions: Transactions,
                                            newTransactions: Transactions) = oldTransactions == newTransactions
        }
    }
}