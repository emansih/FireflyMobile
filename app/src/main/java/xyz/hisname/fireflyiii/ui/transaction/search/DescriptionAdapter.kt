package xyz.hisname.fireflyiii.ui.transaction.search

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.budget_list_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.extension.inflate

class DescriptionAdapter(private val clickListener:(String) -> Unit):
        PagingDataAdapter<String, DescriptionAdapter.DescriptionViewHolder>(DIFF_CALLBACK) {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DescriptionViewHolder {
        context = parent.context
        // TODO: Rename the layout
        return DescriptionViewHolder(parent.inflate(R.layout.budget_list_item))
    }

    override fun onBindViewHolder(holder: DescriptionViewHolder, position: Int){
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }

    inner class DescriptionViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(description: String, clickListener: (String) -> Unit){
            itemView.budgetNameText.text = description
            itemView.setOnClickListener {clickListener(description)}
        }
    }


    companion object {
        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldDescription: String,
                                         newDescription: String) =
                    oldDescription == newDescription

            override fun areContentsTheSame(oldDescription: String,
                                            newDescription: String) = oldDescription == newDescription
        }
    }
}