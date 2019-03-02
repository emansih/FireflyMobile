package xyz.hisname.fireflyiii.ui.budget

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.budget_list_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData
import xyz.hisname.fireflyiii.ui.base.DiffUtilAdapter
import xyz.hisname.fireflyiii.util.extension.inflate

class BudgetRecyclerAdapter(private val items: MutableList<BudgetListData>, private val clickListener:(BudgetListData) -> Unit):
        DiffUtilAdapter<BudgetListData, BudgetRecyclerAdapter.BudgetHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetHolder {
        return BudgetHolder(parent.inflate(R.layout.budget_list_item))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: BudgetHolder, position: Int) = holder.bind(items[position],clickListener)


    inner class BudgetHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(budgetData: BudgetListData, clickListener: (BudgetListData) -> Unit) {
            val budgetAttributes = budgetData.budgetListAttributes
            itemView.budgetNameText.text = budgetAttributes?.name
            itemView.setOnClickListener {clickListener(budgetData)}
        }
    }
}