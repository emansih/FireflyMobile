package xyz.hisname.fireflyiii.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.dialog_search.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.ui.base.BaseDialog
import xyz.hisname.fireflyiii.util.extension.create

class CategoriesDialog: BaseDialog(){

    private var dataAdapter = arrayListOf<CategoryData>()
    private var initialAdapter = arrayListOf<CategoryData>()
    private lateinit var categoriesRecyclerAdapter: CategoriesRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.dialog_search, container)
    }

    private fun displayView(){
        recycler_view.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter?.notifyDataSetChanged()
        }
        val dividerItemDecoration = DividerItemDecoration(recycler_view.context, LinearLayoutManager(requireContext()).orientation)
        recycler_view.addItemDecoration(dividerItemDecoration)
        swipeContainer.isRefreshing = true
        categoryViewModel.getAllCategory().observe(this, Observer { categoryData ->
            categoryViewModel.isLoading.observe(this, Observer { loading ->
                if (loading == false) {
                    swipeContainer.isRefreshing = false
                    categoryData.forEachIndexed { _, catData ->
                        initialAdapter.add(catData)
                        dataAdapter.add(catData)
                    }
                    categoriesRecyclerAdapter = CategoriesRecyclerAdapter(dataAdapter) { data: CategoryData -> itemClicked(data) }
                    recycler_view.adapter = categoriesRecyclerAdapter
                }
            })
        })
    }

    private fun searchData(){
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean{
                categoryViewModel.postCategoryName(query)
                dialog?.dismiss()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if(newText.isBlank() or newText.isEmpty()){
                    categoriesRecyclerAdapter = CategoriesRecyclerAdapter(initialAdapter) { data: CategoryData -> itemClicked(data) }
                    categoriesRecyclerAdapter.notifyDataSetChanged()
                    recycler_view.adapter = categoriesRecyclerAdapter
                } else {
                    filter(newText)
                }
                return true
            }

        })
    }

    private fun filter(categoryName: String){
        dataAdapter.clear()
        categoryViewModel.getCategoryByName(categoryName).observe(this, Observer { categoryData ->
            categoryData.forEachIndexed { _, catData ->
                dataAdapter.add(catData)
            }
            categoriesRecyclerAdapter = CategoriesRecyclerAdapter(dataAdapter) { data: CategoryData ->  itemClicked(data)}
            categoriesRecyclerAdapter.notifyDataSetChanged()
            recycler_view.adapter = categoriesRecyclerAdapter

        })
    }

    private fun itemClicked(categoryData: CategoryData){
        categoryViewModel.postCategoryName(categoryData.categoryAttributes?.name)
        dialog?.dismiss()
    }

    override fun setIcons() {
    }

    override fun setWidgets() {
        searchData()
        displayView()
    }

    override fun submitData() {
    }

}