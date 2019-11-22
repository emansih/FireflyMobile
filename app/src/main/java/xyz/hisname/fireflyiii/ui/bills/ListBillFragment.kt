package xyz.hisname.fireflyiii.ui.bills

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_base_list.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.display
import xyz.hisname.fireflyiii.util.extension.hideFab

class ListBillFragment: BaseFragment() {

    private var dataAdapter = ArrayList<BillData>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_base_list, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayView()
        pullToRefresh()
        initFab()
    }

    private fun displayView(){
        swipeContainer.isRefreshing = true
        runLayoutAnimation(recycler_view)
        billViewModel.getAllBills().observe(this) { billList ->
            swipeContainer.isRefreshing = false
            if (billList.isNotEmpty()) {
                listText.isVisible = false
                listImage.isVisible = false
                recycler_view.isVisible = true
                recycler_view.adapter = BillsRecyclerAdapter(billList) { data: BillData -> itemClicked(data) }
            } else {
                listText.text = resources.getString(R.string.no_bills)
                listImage.setImageDrawable(IconicsDrawable(requireContext())
                        .icon(GoogleMaterial.Icon.gmd_insert_emoticon).sizeDp(24))
                listText.isVisible = true
                listImage.isVisible = true
                recycler_view.isVisible = false
            }
        }
    }

    private fun itemClicked(billData: BillData){
        parentFragmentManager.commit {
            replace(R.id.bigger_fragment_container, AddBillFragment().apply {
                arguments = bundleOf("revealX" to fab.width / 2, "revealY" to fab.height / 2, "billId" to billData.billId)
            })
            addToBackStack(null)
        }
    }

    private fun initFab(){
        fab.display {
            fab.isClickable = false
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, AddBillFragment().apply {
                    arguments = bundleOf("revealX" to fab.width / 2, "revealY" to fab.height / 2)
                })
                addToBackStack(null)
            }
            fab.isClickable = true
        }
        recycler_view.hideFab(fab)
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            dataAdapter.clear()
            displayView()
        }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.bill)
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = resources.getString(R.string.bill)
    }

    override fun onDetach() {
        super.onDetach()
        fab.isGone = true
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}