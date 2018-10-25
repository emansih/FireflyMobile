package xyz.hisname.fireflyiii.ui.bills

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.hisname.fireflyiii.R
import kotlinx.android.synthetic.main.bill_detail_list.view.*
import xyz.hisname.fireflyiii.ui.base.DiffUtilAdapter
import xyz.hisname.fireflyiii.util.extension.inflate


class BillDetailRecyclerAdapter(private val data: MutableList<BillDetailData> ):
        DiffUtilAdapter<BillDetailData, BillDetailRecyclerAdapter.BillDetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillDetailViewHolder {
        return BillDetailViewHolder(parent.inflate(R.layout.bill_detail_list))
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: BillDetailViewHolder, position: Int) = holder.bind(data[position])


    inner class BillDetailViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(billData: BillDetailData){
            itemView.billTitle.text = billData.title
            itemView.billSubtext.text = billData.subTitle
            itemView.billImage.setImageDrawable(billData.image)
        }
    }

}

data class BillDetailData(var title: String?,var  subTitle: String?, var image: Drawable?)