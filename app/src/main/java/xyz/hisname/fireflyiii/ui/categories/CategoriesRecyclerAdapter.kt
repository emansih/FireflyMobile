package xyz.hisname.fireflyiii.ui.categories

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.category_list_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.ui.base.DiffUtilAdapter
import xyz.hisname.fireflyiii.util.extension.inflate

class CategoriesRecyclerAdapter(private val items: MutableList<CategoryData>, private val clickListener:(CategoryData) -> Unit):
        DiffUtilAdapter<CategoryData, CategoriesRecyclerAdapter.CategoryHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        context = parent.context
        return CategoryHolder(parent.inflate(R.layout.category_list_item))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) = holder.bind(items[position],clickListener)

    inner class CategoryHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(categoryData: CategoryData, clickListener: (CategoryData) -> Unit) {
            val categoryDataSet = categoryData.categoryAttributes
            itemView.categoryName.text = categoryDataSet?.name
        }
    }
}