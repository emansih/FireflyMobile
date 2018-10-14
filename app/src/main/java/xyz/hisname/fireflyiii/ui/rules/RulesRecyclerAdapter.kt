package xyz.hisname.fireflyiii.ui.rules

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rules_list.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.rules.RulesData
import xyz.hisname.fireflyiii.ui.base.DiffUtilAdapter
import xyz.hisname.fireflyiii.util.extension.inflate

class RulesRecyclerAdapter(private val items: MutableList<RulesData>) : DiffUtilAdapter<RulesData, RulesRecyclerAdapter.RulesViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RulesViewHolder {
        return RulesViewHolder(parent.inflate(R.layout.rules_list))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RulesViewHolder, position: Int) = holder.bind(items[position])


    inner class RulesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(rulesData: RulesData){
            val rules = rulesData.attributes.title
            itemView.rulesNameTextView.text = rules
        }
    }
}
