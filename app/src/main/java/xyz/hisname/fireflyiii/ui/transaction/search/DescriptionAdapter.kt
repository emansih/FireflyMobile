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

    private lateinit var context: Context
    private var descriptionListItemBinding: DescriptionListItemBinding? = null
    private val binding get() = descriptionListItemBinding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DescriptionViewHolder {
        context = parent.context
        descriptionListItemBinding = DescriptionListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DescriptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DescriptionViewHolder, position: Int){
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }

    inner class DescriptionViewHolder(view: DescriptionListItemBinding): RecyclerView.ViewHolder(view.root) {
        fun bind(description: String, clickListener: (String) -> Unit){
            binding.description.text = description
            binding.root.setOnClickListener {clickListener(description)}
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