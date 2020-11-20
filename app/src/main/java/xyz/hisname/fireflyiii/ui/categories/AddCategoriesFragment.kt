package xyz.hisname.fireflyiii.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_add_category.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.category.CategorySuccessModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.util.extension.*


class AddCategoriesFragment: BottomSheetDialogFragment() {

    private val progressLayout by bindView<View>(R.id.progress_overlay)
    private val categoryViewModel by lazy { getImprovedViewModel(AddCategoryViewModel::class.java) }
    private val categoryId by lazy { arguments?.getLong("categoryId")  ?: 0 }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_category, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidgets()
        if(categoryId != 0L){
            categoryViewModel.getCategoryById(categoryId).observe(viewLifecycleOwner){ categoryData ->
                category_name.setText(categoryData.categoryAttributes?.name)
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
        submitCategory.setOnClickListener {
            hideKeyboard()
            ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            if(categoryId != 0L){
                updateData()
            } else {
                submitData()
            }

        }
    }

    private fun updateData(){
        categoryViewModel.updateCategory(categoryId, category_name.getString()).observe(viewLifecycleOwner){ apiResponse ->
            setResponse(apiResponse)
        }
    }

    private fun submitData(){
        categoryViewModel.addCategory(category_name.getString()).observe(viewLifecycleOwner) { apiResponse ->
            setResponse(apiResponse)
        }
    }

    private fun setResponse(apiResponse: ApiResponses<CategorySuccessModel>){
        ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
        val errorMessage = apiResponse.getErrorMessage()
        when {
            errorMessage != null -> {
                toastError(errorMessage)
            }
            apiResponse.getError() != null -> {
                toastError(apiResponse.getError()?.localizedMessage)
            }
            apiResponse.getResponse() != null -> {
                toastSuccess(requireContext().getString(R.string.category_added, category_name.getString()))
                this.dismiss()
            }
        }
    }
}