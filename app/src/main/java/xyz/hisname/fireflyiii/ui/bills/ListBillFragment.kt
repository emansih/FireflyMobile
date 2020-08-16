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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.bills_list_item.view.*
import kotlinx.android.synthetic.main.fragment_base_list.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.bills.BillsViewModel
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.EndlessRecyclerViewScrollListener
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getViewModel

class ListBillFragment: BaseFragment() {

    private var dataAdapter = arrayListOf<BillData>()
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private val billAdapter by lazy { BillsRecyclerAdapter(dataAdapter) { data: BillData -> itemClicked(data)}  }
    private val linearLayoutManager by lazy { LinearLayoutManager(requireContext()) }
    private val billViewModel by lazy { getViewModel(BillsViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_base_list, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.layoutManager = linearLayoutManager
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recycler_view.adapter = billAdapter
        displayView()
        pullToRefresh()
        initFab()
        enableDragDrop()
    }

    private fun enableDragDrop(){
        recycler_view.enableDragDrop(extendedFab) { viewHolder, isCurrentlyActive ->
            if (viewHolder.itemView.billCard.isOverlapping(extendedFab)){
                extendedFab.dropToRemove()
                if(!isCurrentlyActive){
                    val billName = viewHolder.itemView.billName.text.toString()
                    billViewModel.deleteBillByName(billName).observe(viewLifecycleOwner){ isDeleted ->
                        if(isDeleted){
                            toastSuccess(resources.getString(R.string.bill_deleted, billName))
                        } else {
                            toastOffline("$billName will be deleted later...")
                        }
                    }
                }
            }
        }
    }

    private fun displayView(){
        val dateToRetrieve = DateTimeUtil.getTodayDate()
        swipeContainer.isRefreshing = true
        billViewModel.getPaginatedBills(1, dateToRetrieve, dateToRetrieve).observe(viewLifecycleOwner) { billList ->
            swipeContainer.isRefreshing = false
            if (billList.isNotEmpty()) {
                listText.isVisible = false
                listImage.isVisible = false
                recycler_view.isVisible = true
                dataAdapter.addAll(billList)
                billAdapter.update(dataAdapter)
                billAdapter.notifyDataSetChanged()
            } else {
                listText.text = resources.getString(R.string.no_bills)
                listImage.setImageDrawable(IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_insert_emoticon
                    sizeDp = 24
                })
                listText.isVisible = true
                listImage.isVisible = true
                recycler_view.isVisible = false
            }
        }
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                // Don't load more when data is refreshing
                if(!swipeContainer.isRefreshing) {
                    swipeContainer.isRefreshing = true
                    billViewModel.getPaginatedBills(page + 1, dateToRetrieve, dateToRetrieve).observe(viewLifecycleOwner) { billList ->
                        dataAdapter.clear()
                        dataAdapter.addAll(billList)
                        billAdapter.update(dataAdapter)
                        billAdapter.notifyDataSetChanged()
                        swipeContainer.isRefreshing = false
                    }
                }
            }
        }
        recycler_view.addOnScrollListener(scrollListener)
    }

    private fun itemClicked(billData: BillData){
        parentFragmentManager.commit {
            replace(R.id.bigger_fragment_container, AddBillFragment().apply {
                arguments = bundleOf("revealX" to extendedFab.width / 2, "revealY" to extendedFab.height / 2, "billId" to billData.billId)
            })
            addToBackStack(null)
        }
    }

    private fun initFab(){
        extendedFab.display {
            extendedFab.isClickable = false
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, AddBillFragment().apply {
                    arguments = bundleOf("revealX" to extendedFab.width / 2, "revealY" to extendedFab.height / 2)
                })
                addToBackStack(null)
            }
            extendedFab.isClickable = true
        }
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            dataAdapter.clear()
            scrollListener.resetState()
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
        extendedFab.isGone = true
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}