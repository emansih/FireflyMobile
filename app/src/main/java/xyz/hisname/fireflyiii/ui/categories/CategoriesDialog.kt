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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.dialog_search.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.ui.base.BaseDialog
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel

class CategoriesDialog: BaseDialog(){

    private val categoryAdapter by lazy { CategoriesRecyclerAdapter { data: CategoryData -> itemClicked(data) }  }
    private val categoryViewModel by lazy { getViewModel(CategoriesDialogViewModel::class.java) }
    private lateinit var initialData: PagingData<CategoryData>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.dialog_search, container)
    }

    private fun setRecyclerView(){
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recycler_view.adapter = categoryAdapter
    }

    private fun displayView(){
        categoryViewModel.getCategoryList().observe(viewLifecycleOwner){ pagingData ->
            initialData = pagingData
            categoryAdapter.submitData(lifecycle, pagingData)
        }
    }

    private fun searchData(){
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean{
                categoryViewModel.categoryName.postValue(query)
                dialog?.dismiss()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if(newText.isBlank() or newText.isEmpty()){
                    categoryAdapter.submitData(lifecycle, initialData)
                } else {
                    categoryViewModel.searchCategoryList(newText).observe(viewLifecycleOwner){ data ->
                        categoryAdapter.submitData(lifecycle, data)
                    }
                }
                return true
            }

        })
    }

    private fun itemClicked(categoryData: CategoryData){
        categoryViewModel.categoryName.postValue(categoryData.categoryAttributes.name)
        dialog?.dismiss()
    }

    override fun setWidgets() {
        setRecyclerView()
        searchData()
        displayView()
    }

}