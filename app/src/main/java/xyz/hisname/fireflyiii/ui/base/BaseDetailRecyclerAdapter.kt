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

package xyz.hisname.fireflyiii.ui.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.hisname.fireflyiii.databinding.BaseDetailListBinding
import xyz.hisname.fireflyiii.repository.models.DetailModel

class BaseDetailRecyclerAdapter(private val data: List<DetailModel>,
                                private val clickListener:(position: Int) -> Unit ):
        RecyclerView.Adapter<BaseDetailRecyclerAdapter.BaseDetailViewHolder>() {

    private var baseDetailListBinding: BaseDetailListBinding? = null
    private val binding get() = baseDetailListBinding!!


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseDetailViewHolder {
        return BaseDetailViewHolder(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: BaseDetailViewHolder, position: Int) = holder.bind(data[position], position)


    inner class BaseDetailViewHolder(itemView: BaseDetailListBinding): RecyclerView.ViewHolder(itemView.root){
        fun bind(baseData: DetailModel, clickListener: Int){
            binding.detailTitle.text = baseData.title
            binding.detailSubtext.text = baseData.subTitle
            itemView.setOnClickListener { clickListener(clickListener) }
        }
    }

}

