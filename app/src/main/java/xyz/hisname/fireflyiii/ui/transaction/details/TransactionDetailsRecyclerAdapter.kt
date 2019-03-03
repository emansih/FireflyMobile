package xyz.hisname.fireflyiii.ui.transaction.details

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.transaction_details.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.DetailModel
import xyz.hisname.fireflyiii.ui.base.DiffUtilAdapter
import xyz.hisname.fireflyiii.util.extension.inflate

class TransactionDetailsRecyclerAdapter(private val items: MutableList<DetailModel>,
                                        private val clickListener:(position: Int) -> Unit):
        DiffUtilAdapter<DetailModel, TransactionDetailsRecyclerAdapter.TransactionInfoAdapter>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionInfoAdapter {
        return TransactionInfoAdapter(parent.inflate(R.layout.transaction_details))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: TransactionInfoAdapter, position: Int) = holder.bind(items[position], position)



    inner class TransactionInfoAdapter(view: View): RecyclerView.ViewHolder(view) {
        fun bind(transactionInfo: DetailModel, clickListener: Int){
            itemView.infotext.text = transactionInfo.title
            val transactionSubTitle = transactionInfo.subTitle
            if(transactionSubTitle != null && transactionSubTitle.isNotEmpty() && transactionSubTitle.isNotBlank() &&
                    transactionSubTitle.length >= 15){
                itemView.infotext_content.text = transactionSubTitle.substring(0,15) + "..."
            } else {
                itemView.infotext_content.text = transactionSubTitle
            }
            itemView.setOnClickListener { clickListener(clickListener) }
        }
    }
}