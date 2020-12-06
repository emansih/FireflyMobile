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
            itemView.categoryName.text = categoryDataSet?.name
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