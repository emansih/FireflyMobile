package xyz.hisname.fireflyiii.ui.piggybank

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.transaction_details.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.DetailModel
import xyz.hisname.fireflyiii.ui.base.DiffUtilAdapter
import xyz.hisname.fireflyiii.util.extension.inflate

class PiggyDetailsRecyclerAdapter(private val items: MutableList<DetailModel>,
                                  private val clickListener:(position: Int) -> Unit):
        DiffUtilAdapter<DetailModel, PiggyDetailsRecyclerAdapter.PiggybankDetails>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PiggybankDetails {
        return PiggybankDetails(parent.inflate(R.layout.transaction_details))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PiggybankDetails, position: Int) = holder.bind(items[position], position)

    inner class PiggybankDetails(view: View): RecyclerView.ViewHolder(view) {
        fun bind(transactionInfo: DetailModel, clickListener: Int){
            itemView.infotext.text = transactionInfo.title
            val transactionSubTitle = transactionInfo.subTitle
            itemView.infotext_content.text = transactionSubTitle
            itemView.setOnClickListener { clickListener(clickListener) }
        }
    }
}