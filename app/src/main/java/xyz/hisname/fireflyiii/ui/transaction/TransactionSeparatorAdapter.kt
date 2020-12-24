package xyz.hisname.fireflyiii.ui.transaction

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recent_transaction_list.view.*
import kotlinx.android.synthetic.main.transaction_item_separator.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.models.transaction.SplitSeparator
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.getCompatColor
import kotlin.math.abs

class TransactionSeparatorAdapter(private val clickListener:(Transactions) -> Unit):
        PagingDataAdapter<SplitSeparator, RecyclerView.ViewHolder>(DIFF_CALLBACK){


    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return when(viewType){
            R.layout.recent_transaction_list -> {
                TransactionViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.recent_transaction_list, parent, false))
            }
            else -> {
                SplitSeparatorViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.transaction_item_separator, parent, false))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is SplitSeparator.TransactionItem -> R.layout.recent_transaction_list
            is SplitSeparator.SeparatorItem -> R.layout.transaction_item_separator
            null -> throw UnsupportedOperationException("Unknown view")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int){
        val transactionModel = getItem(position)
        transactionModel.let { separator ->
            when(separator){
                is SplitSeparator.SeparatorItem -> {
                    val viewHolder = holder as SplitSeparatorViewHolder
                    viewHolder.itemView.separator_description.text = separator.description
                }
                is SplitSeparator.TransactionItem -> {
                    val viewHolder = holder as TransactionViewHolder
                    val timePreference = AppPref(PreferenceManager.getDefaultSharedPreferences(context)).timeFormat
                    val transactionDescription = separator.transaction.description
                    val descriptionText = if(separator.transaction.isPending){
                        viewHolder.itemView.transactionNameText.setTextColor(context.getCompatColor(R.color.md_red_500))
                        "$transactionDescription (Pending)"
                    } else {
                        transactionDescription
                    }
                    viewHolder.itemView.transactionNameText.text = descriptionText
                    viewHolder.itemView.sourceNameText.text = separator.transaction.source_name
                    viewHolder.itemView.transactionJournalId.text = separator.transaction.transaction_journal_id.toString()
                    viewHolder.itemView.dateText.text = DateTimeUtil.convertLocalDateTime(separator.transaction.date,
                            timePreference)
                    if(separator.transaction.amount.toString().startsWith("-")){
                        // Negative value means it's a withdrawal
                        viewHolder.itemView.transactionAmountText.setTextColor(context.getCompatColor(R.color.md_red_500))
                        viewHolder.itemView.transactionAmountText.text = "-" + separator.transaction.currency_symbol +
                                abs(separator.transaction.amount)
                    } else {
                        viewHolder.itemView.transactionAmountText.text = separator.transaction.currency_symbol +
                                separator.transaction.amount.toString()
                    }
                    viewHolder.itemView.list_item.setOnClickListener {clickListener(separator.transaction)}
                }
            }
        }
    }


    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class SplitSeparatorViewHolder(view: View) : RecyclerView.ViewHolder(view)


    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<SplitSeparator>() {
            override fun areItemsTheSame(oldTransactions: SplitSeparator,
                                         newTransactions: SplitSeparator) =
                    oldTransactions == newTransactions

            override fun areContentsTheSame(oldTransactions: SplitSeparator,
                                            newTransactions: SplitSeparator) = oldTransactions == newTransactions
        }
    }
}