package xyz.hisname.fireflyiii.ui.categories

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_base_list.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.toastError
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.util.extension.display
import xyz.hisname.fireflyiii.util.extension.hideFab


class CategoriesFragment: BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_base_list, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayView()
        initFab()
        pullToRefresh()
    }

    private fun displayView(){
        runLayoutAnimation(recycler_view)
        swipeContainer.isRefreshing = true
        categoryViewModel.getAllCategory().observe(this) { categoryData ->
            swipeContainer.isRefreshing = false
            if (categoryData.isNotEmpty()) {
                listText.isVisible = false
                listImage.isVisible = false
                recycler_view.isVisible = true
                recycler_view.adapter = CategoriesRecyclerAdapter(categoryData) { data: CategoryData ->  }
            } else {
                listText.text = "No category found"
                listText.isVisible = true
                listImage.isVisible = true
                listImage.setImageDrawable(IconicsDrawable(requireContext())
                        .icon(FontAwesome.Icon.faw_chart_bar)
                        .sizeDp(24))
                recycler_view.isVisible = false
            }
        }
    }

    private fun initFab(){
        fab.display {
            fab.isClickable = false
            val addCategoryFragment = AddCategoriesFragment()
            addCategoryFragment.show(parentFragmentManager, "add_category_fragment")
            fab.isClickable = true
        }
        recycler_view.hideFab(fab)
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            displayView()
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

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}