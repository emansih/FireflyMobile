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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.FragmentAddCategoryBinding
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.util.extension.*


class AddCategoriesFragment: BottomSheetDialogFragment() {

    private val categoryViewModel by lazy { getImprovedViewModel(AddCategoryViewModel::class.java) }
    private val categoryId by lazy { arguments?.getLong("categoryId")  ?: 0 }
    private val progressLayout by bindView<View>(R.id.progress_overlay)
    private var fragmentAddCategoryBinding: FragmentAddCategoryBinding? = null
    private val binding get() = fragmentAddCategoryBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentAddCategoryBinding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidgets()
        if(categoryId != 0L){
            categoryViewModel.getCategoryById(categoryId).observe(viewLifecycleOwner){ categoryData ->
                binding.categoryName.setText(categoryData.categoryAttributes.name)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val addCatDialog = dialog
        if (addCatDialog != null) {
            val bottomSheet = addCatDialog.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet.layoutParams.height = getScreenHeight().div(1.4.toInt())
        }
    }

    private fun setWidgets(){
        binding.submitCategory.setOnClickListener {
            hideKeyboard()
            if(categoryId != 0L){
                updateData()
            } else {
                submitData()
            }
        }
        categoryViewModel.isLoading.observe(viewLifecycleOwner){ loader ->
            if(loader){
                ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }

        }
    }

    private fun updateData(){
        categoryViewModel.updateCategory(categoryId, binding.categoryName.getString()).observe(viewLifecycleOwner){ response ->
            if(response.first){
                toastSuccess(response.second)
                dismiss()
            } else {
                toastInfo(response.second)
            }
        }
    }

    private fun submitData(){
        categoryViewModel.addCategory(binding.categoryName.getString()).observe(viewLifecycleOwner) { response ->
            if(response.first){
                toastSuccess(response.second)
                dismiss()
            } else {
                toastInfo(response.second)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentAddCategoryBinding = null
    }
}