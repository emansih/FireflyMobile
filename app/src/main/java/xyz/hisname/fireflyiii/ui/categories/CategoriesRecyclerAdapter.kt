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
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.category_list_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.util.extension.inflate

class CategoriesRecyclerAdapter(private val clickListener:(CategoryData) -> Unit):
        PagingDataAdapter<CategoryData, CategoriesRecyclerAdapter.CategoryHolder>(DIFF_CALLBACK) {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        context = parent.context
        return CategoryHolder(parent.inflate(R.layout.category_list_item))
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int){
        getItem(position)?.let{
            holder.bind(it, clickListener)
        }
    }

    inner class CategoryHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(categoryData: CategoryData, clickListener: (CategoryData) -> Unit) {
            val categoryDataSet = categoryData.categoryAttributes
            itemView.categoryName.text = categoryDataSet.name
            itemView.categoryId.text = categoryData.categoryId.toString()
            itemView.setOnClickListener { clickListener(categoryData) }
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