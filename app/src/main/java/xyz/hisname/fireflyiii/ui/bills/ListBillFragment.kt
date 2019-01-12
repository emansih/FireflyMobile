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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_base_list.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.display
import xyz.hisname.fireflyiii.util.extension.toastError

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
        runLayoutAnimation(recycler_view)
        billViewModel.getAllBills().observe(this, Observer { billList ->
            billViewModel.isLoading.observe(this, Observer { loader ->
                if(loader == false){
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
            })

        })
        billViewModel.isLoading.observe(this, Observer {
            swipeContainer.isRefreshing = it == true
        })
        billViewModel.apiResponse.observe(this, Observer {
           toastError(it)
        })
    }

    private fun itemClicked(billData: BillData){
        requireFragmentManager().commit {
            replace(R.id.bigger_fragment_container, AddBillFragment())
            arguments = bundleOf("revealX" to fab.width / 2, "revealY" to fab.height / 2, "billId" to billData.billId)
        }
    }

    private fun initFab(){
        fab.display {
            fab.isClickable = false
            requireFragmentManager().commit {
                replace(R.id.bigger_fragment_container, AddBillFragment())
                arguments = bundleOf("revealX" to fab.width / 2, "revealY" to fab.height / 2)
            }
            fab.isClickable = true
        }
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                 if(dy > 0 && fab.isShown){
                     fab.hide()
                 }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    fab.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            dataAdapter.clear()
            displayView()
        }
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)
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
}