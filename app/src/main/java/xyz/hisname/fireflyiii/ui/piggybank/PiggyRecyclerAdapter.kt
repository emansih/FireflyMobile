package xyz.hisname.fireflyiii.ui.piggybank

import android.content.Context
import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.piggy_list_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.ui.base.DiffUtilAdapter
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.inflate

class PiggyRecyclerAdapter(private val items: MutableList<PiggyData>, private val clickListener:(PiggyData) -> Unit):
        DiffUtilAdapter<PiggyData,PiggyRecyclerAdapter.PiggyHolder>(){

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PiggyHolder {
        context = parent.context
        return PiggyHolder(parent.inflate(R.layout.piggy_list_item))
    }

    override fun onBindViewHolder(holder: PiggyHolder, position: Int) = holder.bind(items[position],clickListener)


    override fun getItemCount() = items.size

    inner class PiggyHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(piggyData: PiggyData, clickListener: (PiggyData) -> Unit){
            val piggyBankData = piggyData.piggyAttributes
            itemView.piggyName.text = piggyBankData?.name
            if(piggyBankData?.name != null){
                if(piggyBankData.name.length >= 19){
                    itemView.piggyName.text = piggyBankData.name.substring(0,19) + "..."
                } else {
                    itemView.piggyName.text = piggyBankData.name
                }
            }
            itemView.goal_save.text = piggyBankData?.currency_symbol + " " + piggyBankData?.target_amount
            itemView.currently_saved.text = piggyBankData?.currency_symbol + " " + piggyBankData?.current_amount.toString()
            if(piggyBankData!!.percentage <= 15){
                itemView.goal_progress_bar.progressDrawable.setColorFilter(ContextCompat.getColor(context,R.color.md_red_700),
                        PorterDuff.Mode.SRC_IN)
            } else if(piggyBankData.percentage <= 50){
                itemView.goal_progress_bar.progressDrawable.setColorFilter(ContextCompat.getColor(context,R.color.md_green_500),
                        PorterDuff.Mode.SRC_IN)
            }
            itemView.goal_progress_bar.progress = piggyBankData.percentage
            val targetDate = piggyBankData.target_date
            itemView.timeLeft.let {
                if(targetDate != null){
                    if(piggyBankData.percentage != 100){
                        val daysDiff = DateTimeUtil.getDaysDifference(targetDate).toInt()
                        when{
                            daysDiff == 0 -> it.text = context.getString(R.string.due_today)
                            daysDiff == 1 -> it.text = context.getString(R.string.one_more_day_to_target)
                            daysDiff < 0 -> {
                                val inverseMath = Math.abs(daysDiff)
                                it.text =  context.getString(R.string.target_missed,inverseMath)
                            }
                            daysDiff == -1 -> it.text = context.getString(R.string.yesterday_target)
                            daysDiff >= 0 -> it.text = context.getString(R.string.days_to_go, daysDiff)

                        }
                    } else {
                        it.text = context.getString(R.string.user_did_it)
                    }
                } else {
                    it.text = "No target Date"
                }
            }
            itemView.piggyCard.setOnClickListener{clickListener(piggyData)}
        }
    }
}