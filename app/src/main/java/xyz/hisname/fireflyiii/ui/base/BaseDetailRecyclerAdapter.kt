package xyz.hisname.fireflyiii.ui.base

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.base_detail_list.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.DetailModel
import xyz.hisname.fireflyiii.util.extension.inflate

class BaseDetailRecyclerAdapter(private val data: MutableList<DetailModel>,
                                private val clickListener:(position: Int) -> Unit ):
        DiffUtilAdapter<DetailModel, BaseDetailRecyclerAdapter.BaseDetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseDetailViewHolder {
        return BaseDetailViewHolder(parent.inflate(R.layout.base_detail_list))
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: BaseDetailViewHolder, position: Int) = holder.bind(data[position], position)


    inner class BaseDetailViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(baseData: DetailModel, clickListener: Int){
            itemView.detailTitle.text = baseData.title
            itemView.detailSubtext.text = baseData.subTitle
            itemView.setOnClickListener { clickListener(clickListener) }
        }
    }

}

