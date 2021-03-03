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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.sizeDp
import xyz.hisname.fireflyiii.databinding.BaseSwipeLayoutBinding
import xyz.hisname.fireflyiii.databinding.CategoryListItemBinding
import xyz.hisname.fireflyiii.databinding.FragmentBaseListBinding
import xyz.hisname.fireflyiii.util.extension.*


class CategoriesFragment: BaseFragment() {

    private val categoryAdapter by lazy { CategoriesRecyclerAdapter { data: CategoryData -> itemClicked(data) }  }
    private val categoryViewModel by lazy { getImprovedViewModel(CategoryListViewModel::class.java) }
    private var fragmentBaseListBinding: FragmentBaseListBinding? = null
    private val binding get() = fragmentBaseListBinding!!
    private var baseSwipeLayoutBinding: BaseSwipeLayoutBinding? = null
    private val baseSwipeBinding get() = baseSwipeLayoutBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentBaseListBinding = FragmentBaseListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayView()
        initFab()
        pullToRefresh()
        baseSwipeBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        baseSwipeBinding.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        baseSwipeBinding.recyclerView.adapter = categoryAdapter
        enableDragAndDrop()
    }

    private fun enableDragAndDrop(){
        baseSwipeBinding.recyclerView.enableDragDrop(extendedFab) { viewHolder, isCurrentlyActive ->
            val categoryListBinding = CategoryListItemBinding.bind(viewHolder.itemView)
            if (categoryListBinding.categoryList.isOverlapping(extendedFab)){
                extendedFab.dropToRemove()
                if(!isCurrentlyActive){
                    val categoryName = categoryListBinding.categoryName.text.toString()
                    val categoryId = categoryListBinding.categoryId.text.toString()
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
            baseSwipeBinding.swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
            if(loadStates.refresh !is LoadState.Loading) {
                if (categoryAdapter.itemCount < 1) {
                    baseSwipeBinding.recyclerView.isGone = true
                    binding.listImage.isVisible = true
                    binding.listImage.setImageDrawable(IconicsDrawable(requireContext()).apply {
                        icon = FontAwesome.Icon.faw_list
                        sizeDp = 24
                    })
                    binding.listText.isVisible = true
                    binding.listText.text = "No Categories Found"
                } else {
                    baseSwipeBinding.recyclerView.isVisible = true
                    binding.listImage.isGone = true
                    binding.listText.isGone = true
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
        baseSwipeBinding.swipeContainer.setOnRefreshListener {
            categoryAdapter.refresh()
        }
    }


    override fun onAttach(context: Context){
        super.onAttach(context)
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = resources.getString(R.string.categories)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = resources.getString(R.string.categories)
    }
}