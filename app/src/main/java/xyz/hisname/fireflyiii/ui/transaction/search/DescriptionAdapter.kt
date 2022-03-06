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

package xyz.hisname.fireflyiii.ui.transaction.search

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import xyz.hisname.fireflyiii.databinding.DescriptionListItemBinding

class DescriptionAdapter(private val clickListener:(String) -> Unit):
        PagingDataAdapter<String, DescriptionAdapter.DescriptionViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DescriptionViewHolder {
        val context = parent.context
        val itemView = DescriptionListItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return DescriptionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DescriptionViewHolder, position: Int){
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }

    inner class DescriptionViewHolder(
        private val view: DescriptionListItemBinding
    ): RecyclerView.ViewHolder(view.root) {
        fun bind(description: String, clickListener: (String) -> Unit){
            view.description.text = description
            view.root.setOnClickListener {clickListener(description)}
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