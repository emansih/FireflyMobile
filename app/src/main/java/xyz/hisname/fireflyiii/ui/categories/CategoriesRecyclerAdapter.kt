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

package xyz.hisname.fireflyiii.ui.categories

import android.content.Context
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import xyz.hisname.fireflyiii.databinding.CategoryListItemBinding
import xyz.hisname.fireflyiii.repository.models.category.CategoryData

class CategoriesRecyclerAdapter(private val clickListener:(CategoryData) -> Unit):
        PagingDataAdapter<CategoryData, CategoriesRecyclerAdapter.CategoryHolder>(DIFF_CALLBACK) {

    private lateinit var context: Context
    private var categoryListItemBinding: CategoryListItemBinding? = null
    private val binding get() = categoryListItemBinding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        context = parent.context
        return CategoryHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int){
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }

    inner class CategoryHolder(itemView: CategoryListItemBinding): RecyclerView.ViewHolder(itemView.root) {
        fun bind(categoryData: CategoryData, clickListener: (CategoryData) -> Unit) {
            val categoryDataSet = categoryData.categoryAttributes
            binding.categoryName.text = categoryDataSet.name
            binding.categoryId.text = categoryData.categoryId.toString()
            binding.root.setOnClickListener { clickListener(categoryData) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<CategoryData>() {
            override fun areItemsTheSame(oldCategoryData: CategoryData,
                                         newCategoryData: CategoryData) = oldCategoryData == newCategoryData

            override fun areContentsTheSame(oldCategoryData: CategoryData,
                                            newCategoryData: CategoryData) = oldCategoryData == newCategoryData
        }
    }
}