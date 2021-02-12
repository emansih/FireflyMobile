/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.ui.piggybank

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.piggy_list_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.getCompatColor
import xyz.hisname.fireflyiii.util.extension.inflate
import kotlin.math.abs

class PiggyRecyclerAdapter(private val clickListener:(PiggyData) -> Unit):
        PagingDataAdapter<PiggyData, PiggyRecyclerAdapter.PiggyHolder>(DIFF_CALLBACK){

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PiggyHolder {
        context = parent.context
        return PiggyHolder(parent.inflate(R.layout.piggy_list_item))
    }

    override fun onBindViewHolder(holder: PiggyHolder, position: Int){
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }


    inner class PiggyHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(piggyData: PiggyData, clickListener: (PiggyData) -> Unit){
            val piggyBankData = piggyData.piggyAttributes
            var piggyBankName = piggyBankData.name
            val isPending = piggyBankData.isPending
            if(piggyBankName.length >= 17){
                piggyBankName = piggyBankName.substring(0,17) + "..."
            }
            if(isPending){
                itemView.piggyName.setTextColor(context.getCompatColor(R.color.md_red_500))
                piggyBankName = "$piggyBankName (Pending)"
            }
            itemView.piggyName.text = piggyBankName

            itemView.goal_save.text = piggyBankData?.currency_symbol + " " + piggyBankData?.target_amount
            itemView.currently_saved.text = piggyBankData?.currency_symbol + " " + piggyBankData?.current_amount.toString()
            val percentage = piggyBankData.percentage ?: 0
            if(percentage <= 15){
                itemView.goal_progress_bar.progressDrawable.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(context.getCompatColor(R.color.md_red_700),
                                BlendModeCompat.SRC_ATOP)
            } else if(percentage <= 50){
                itemView.goal_progress_bar.progressDrawable.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(context.getCompatColor(R.color.md_green_500),
                        BlendModeCompat.SRC_ATOP)
            }
            itemView.goal_progress_bar.progress = percentage
            val targetDate = piggyBankData.target_date
            itemView.timeLeft.let {
                if(!targetDate.isNullOrBlank()){
                    if(piggyBankData.percentage != 100){
                        val daysDiff = DateTimeUtil.getDaysDifference(targetDate).toInt()
                        when{
                            daysDiff == 0 -> it.text = context.getString(R.string.due_today)
                            daysDiff == 1 -> it.text = context.getString(R.string.one_more_day_to_target)
                            daysDiff < 0 -> {
                                val inverseMath = abs(daysDiff)
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
            itemView.piggyId.text = piggyData.piggyId.toString()
            itemView.piggyCard.setOnClickListener{clickListener(piggyData)}
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<PiggyData>() {
            override fun areItemsTheSame(oldPiggyData: PiggyData,
                                         newPiggyData: PiggyData) = oldPiggyData == newPiggyData

            override fun areContentsTheSame(oldPiggyData: PiggyData,
                                            newPiggyDataa: PiggyData) = oldPiggyData == newPiggyDataa
        }
    }
}