package xyz.hisname.fireflyiii.ui.piggybank

import android.content.Context
import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
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

    override fun onBindViewHolder(holder: PiggyHolder, position: Int) {
        val piggyData = items[position].piggyAttributes
        holder.piggyName.text = piggyData?.name
        holder.goalAmount.text = piggyData?.currency_symbol + " " + piggyData?.target_amount
        holder.amountSaved.text = piggyData?.currency_symbol + " " + piggyData?.current_amount.toString()
        if(piggyData!!.percentage <= 15.toDouble()){
            holder.piggyProgress.progressDrawable.setColorFilter(ContextCompat.getColor(context,R.color.md_red_700),
                    PorterDuff.Mode.SRC_IN)
        } else if(piggyData.percentage <= 50.toDouble()){
            holder.piggyProgress.progressDrawable.setColorFilter(ContextCompat.getColor(context,R.color.md_green_500),
                    PorterDuff.Mode.SRC_IN)
        }
        holder.piggyProgress.progress = piggyData.percentage.toInt()
        val targetDate = piggyData.target_date
        holder.timeLeft.let {
            if(targetDate != null){
                if(piggyData.percentage.toInt() != 100){
                    val daysDiff = DateTimeUtil.getDaysDifference(targetDate).toInt()
                    when{
                        daysDiff == 0 -> it.text = context.getString(R.string.target_due_today)
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
                it.text = context.getString(R.string.no_target_date)
            }
        }
        holder.piggyId.text = items[position].piggyId.toString()
        holder.piggyId.setOnClickListener{clickListener(items[position])}
    }

    override fun getItemCount() = items.size

    inner class PiggyHolder(view: View): RecyclerView.ViewHolder(view) {
        val piggyName: TextView = view.piggyName
        val amountSaved: TextView = view.currently_saved
        val goalAmount: TextView = view.goal_save
        val piggyProgress: ContentLoadingProgressBar = view.goal_progress_bar
        val timeLeft: TextView = view.timeLeft
        val piggyId: TextView = view.piggyId
    }
}