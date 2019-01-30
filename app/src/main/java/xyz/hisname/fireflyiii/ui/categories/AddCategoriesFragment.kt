package xyz.hisname.fireflyiii.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.fragment_add_category.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseAddObjectFragment
import xyz.hisname.fireflyiii.util.extension.*

class AddCategoriesFragment: BaseAddObjectFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_category, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(addCategoryContainer)
    }

    override fun setIcons() {
        addCategoryFab.setImageDrawable(IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_chart_bar).sizeDp(24))
    }

    override fun setWidgets() {
        placeHolderToolbar.setNavigationOnClickListener {
            handleBack()
        }
        addCategoryFab.setOnClickListener {
            hideKeyboard()
            ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            submitData()
        }
    }

    override fun submitData() {
        categoryViewModel.addCategory(name_edittext.getString()).observe(this, Observer {
            ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            val errorMessage = it.getErrorMessage()
            if (errorMessage != null) {
                toastError(errorMessage)
            } else if (it.getError() != null) {
                toastError(it.getError()?.localizedMessage)
            } else if (it.getResponse() != null) {
                toastSuccess("Category Added")
                unReveal(addCategoryFab)
            }
        })
    }

    override fun handleBack() {
        unReveal(addCategoryContainer)
    }
}