package xyz.hisname.fireflyiii.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.dialog_search.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.category.CategoryViewModel
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.ui.base.BaseDialog
import xyz.hisname.fireflyiii.util.EndlessRecyclerViewScrollListener
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel

class CategoriesDialog: BaseDialog(){

    private var dataAdapter = arrayListOf<CategoryData>()
    private var initialAdapter = arrayListOf<CategoryData>()
    private lateinit var categoriesRecyclerAdapter: CategoriesRecyclerAdapter
    private val categoryViewModel by lazy { getViewModel(CategoryViewModel::class.java) }
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private val linearLayout by lazy { LinearLayoutManager(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.dialog_search, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
    }

    private fun setRecyclerView(){
        val dividerItemDecoration = DividerItemDecoration(recycler_view.context, LinearLayoutManager(requireContext()).orientation)
        recycler_view.apply {
            layoutManager = linearLayout
            adapter?.notifyDataSetChanged()
            addItemDecoration(dividerItemDecoration)
        }
    }

    private fun displayView(){
        swipeContainer.isRefreshing = true
        categoryViewModel.getPaginatedCategory(1).observe(viewLifecycleOwner) { categoryData ->
            swipeContainer.isRefreshing = false
            categoryData.forEachIndexed { _, catData ->
                initialAdapter.add(catData)
                dataAdapter.add(catData)
            }
            categoriesRecyclerAdapter = CategoriesRecyclerAdapter() { data: CategoryData -> itemClicked(data) }
            recycler_view.adapter = categoriesRecyclerAdapter
        }
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayout){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                // Don't load more when data is refreshing
                if(!swipeContainer.isRefreshing && page != 1) {
                    swipeContainer.isRefreshing = true
                    categoryViewModel.getPaginatedCategory(page + 1).observe(this@CategoriesDialog) { catList ->
                        dataAdapter.clear()
                        dataAdapter.addAll(catList)
                     //   categoriesRecyclerAdapter.update(dataAdapter)
                        categoriesRecyclerAdapter.notifyDataSetChanged()
                        swipeContainer.isRefreshing = false
                    }
                }
            }
        }
        recycler_view.addOnScrollListener(scrollListener)
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
               //     categoriesRecyclerAdapter = CategoriesRecyclerAdapter(initialAdapter) { data: CategoryData -> itemClicked(data) }
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
        categoryViewModel.getCategoryByName(categoryName).observe(viewLifecycleOwner) { categoryData ->
            dataAdapter.addAll(categoryData)
        //    categoriesRecyclerAdapter = CategoriesRecyclerAdapter(dataAdapter) { data: CategoryData ->  itemClicked(data)}
            categoriesRecyclerAdapter.notifyDataSetChanged()
            recycler_view.adapter = categoriesRecyclerAdapter
        }
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