package xyz.hisname.fireflyiii.ui.categories

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_base_list.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.category_list_item.view.*
import xyz.hisname.fireflyiii.util.extension.*


class CategoriesFragment: BaseFragment() {

    private val categoryAdapter by lazy { CategoriesRecyclerAdapter { data: CategoryData -> itemClicked(data) }  }
    private val categoryViewModel by lazy { getImprovedViewModel(CategoryListViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_base_list, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayView()
        initFab()
        pullToRefresh()
        recycler_view.layoutManager = linearLayout()
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recycler_view.adapter = categoryAdapter
        enableDragAndDrop()
    }

    private fun enableDragAndDrop(){
        recycler_view.enableDragDrop(extendedFab) { viewHolder, isCurrentlyActive ->
            if (viewHolder.itemView.categoryList.isOverlapping(extendedFab)){
                extendedFab.dropToRemove()
                if(!isCurrentlyActive){
                    val categoryName = viewHolder.itemView.categoryName.text.toString()
                    val categoryId = viewHolder.itemView.categoryId.text.toString()
                    categoryViewModel.deleteCategory(categoryId).observe(viewLifecycleOwner){ isDeleted ->
                        categoryAdapter.refresh()
                        if(isDeleted){
                            toastSuccess("$categoryName deleted")
                        } else {
                            toastOffline(resources.getString(R.string.data_will_be_deleted_later, categoryName), Toast.LENGTH_LONG)
                        }
                    }
                }
            }
        }
    }

    private fun displayView(){
        categoryViewModel.getCategories().observe(viewLifecycleOwner){ pagingData ->
            categoryAdapter.submitData(lifecycle, pagingData)
        }
        categoryAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner) { loadStates ->
            swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
            if(loadStates.refresh !is LoadState.Loading) {
                if (categoryAdapter.itemCount < 1) {
                    recycler_view.isGone = true
                    listImage.isVisible = true
                    listImage.setImageDrawable(IconicsDrawable(requireContext()).apply {
                        icon = FontAwesome.Icon.faw_list
                        sizeDp = 24
                    })
                    listText.isVisible = true
                    listText.text = "No Categories Found"
                } else {
                    recycler_view.isVisible = true
                    listImage.isGone = true
                    listText.isGone = true
                }
            }
        }

    }


    private fun itemClicked(categoryData: CategoryData){
        val bundle = bundleOf("categoryId" to categoryData.categoryId)
        parentFragmentManager.commit {
            replace(R.id.fragment_container, CategoryDetailsFragment().apply { arguments = bundle })
            addToBackStack(null)
        }
        extendedFab.isGone = true
    }

    private fun initFab(){
        extendedFab.display {
            extendedFab.isClickable = false
            val addCategoryFragment = AddCategoriesFragment()
            addCategoryFragment.show(parentFragmentManager, "add_category_fragment")
            extendedFab.isClickable = true
        }
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            categoryAdapter.refresh()
        }
    }


    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.categories)
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = resources.getString(R.string.categories)
    }


    override fun onDetach() {
        super.onDetach()
        extendedFab.isGone = true
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}