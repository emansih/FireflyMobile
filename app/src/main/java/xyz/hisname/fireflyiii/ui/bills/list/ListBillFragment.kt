package xyz.hisname.fireflyiii.ui.bills.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
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
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.bills.AddBillFragment
import xyz.hisname.fireflyiii.ui.bills.details.BillDetailsFragment
import xyz.hisname.fireflyiii.ui.bills.BillsRecyclerAdapter
import xyz.hisname.fireflyiii.util.extension.*

class ListBillFragment: BaseFragment() {

    private val billAdapter by lazy { BillsRecyclerAdapter { data: BillData -> itemClicked(data)}  }
    private val billViewModel by lazy { getImprovedViewModel(ListBillViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_bill_list, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        loadBill()
        pullToRefresh()
        initFab()
    }

    private fun setRecyclerView(){
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.adapter = billAdapter
        recycler_view.enableDragDrop(extendedFab) { viewHolder, isCurrentlyActive ->
            if (viewHolder.itemView.billCard.isOverlapping(extendedFab)){
                extendedFab.dropToRemove()
                if(!isCurrentlyActive){
                    val billId = viewHolder.itemView.billId.text.toString()
                    val billName = viewHolder.itemView.billName.text.toString()
                    billViewModel.deleteBillById(billId).observe(viewLifecycleOwner){ isDeleted ->
                        billAdapter.refresh()
                        if(isDeleted){
                            toastSuccess(resources.getString(R.string.bill_deleted, billName))
                        } else {
                            toastOffline(resources.getString(R.string.data_will_be_deleted_later, billName), Toast.LENGTH_LONG)
                        }
                    }
                }
            }
        }
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && extendedFab.isShown) {
                    extendedFab.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    extendedFab.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    private fun loadBill(){
        billViewModel.getBillList().observe(viewLifecycleOwner){
            billAdapter.submitData(lifecycle, it)
        }
    }

    private fun itemClicked(billData: BillData){
        parentFragmentManager.commit {
            replace(R.id.fragment_container, BillDetailsFragment().apply {
                arguments = bundleOf("billId" to billData.billId)
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
            billAdapter.refresh()
        }
        billAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner){ loadStates ->
            swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
            if(loadStates.refresh !is LoadState.Loading) {
                if(billAdapter.itemCount < 1){
                    listText.text = resources.getString(R.string.no_bills)
                    listImage.setImageDrawable(IconicsDrawable(requireContext()).apply {
                        icon = GoogleMaterial.Icon.gmd_insert_emoticon
                        sizeDp = 24
                    })
                    listText.isVisible = true
                    listImage.isVisible = true
                } else {
                    listText.isVisible = false
                    listImage.isVisible = false
                    recycler_view.isVisible = true
                }
            }
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
}