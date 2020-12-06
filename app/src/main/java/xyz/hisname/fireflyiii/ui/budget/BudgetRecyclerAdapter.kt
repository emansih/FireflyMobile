package xyz.hisname.fireflyiii.ui.budget

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.budget_list_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData
import xyz.hisname.fireflyiii.util.extension.inflate

class BudgetRecyclerAdapter(private val clickListener:(BudgetListData) -> Unit):
        PagingDataAdapter<BudgetListData, BudgetRecyclerAdapter.BudgetHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetHolder {
        return BudgetHolder(parent.inflate(R.layout.budget_list_item))
    }

    override fun onBindViewHolder(holder: BudgetHolder, position: Int){
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }

    inner class BudgetHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(budgetData: BudgetListData, clickListener: (BudgetListData) -> Unit) {
            val budgetAttributes = budgetData.budgetListAttributes
            itemView.budgetNameText.text = budgetAttributes?.name
            itemView.setOnClickListener {clickListener(budgetData)}
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<BudgetListData>() {
            override fun areItemsTheSame(oldBudgetListData: BudgetListData,
                                         newBudgetListData: BudgetListData) = oldBudgetListData == newBudgetListData

            override fun areContentsTheSame(oldBudgetListData: BudgetListData,
                                            newBudgetListData: BudgetListData) = oldBudgetListData == newBudgetListData
        }
    }
}