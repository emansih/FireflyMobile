package xyz.hisname.fireflyiii.ui.bills

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_base_list.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.bills.BillsViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError

class ListBillFragment: BaseFragment() {

    private val billViewModel by lazy { getViewModel(BillsViewModel::class.java) }
    private var dataAdapter = ArrayList<BillData>()
    private var billDatabase: AppDatabase? = null
    private val fab by lazy { requireActivity().findViewById<FloatingActionButton>(R.id.globalFAB) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_base_list, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        billDatabase = AppDatabase.getInstance(requireContext())
        displayView()
        pullToRefresh()
        initFab()
    }

    private fun displayView(){
        runLayoutAnimation(recycler_view)
        billViewModel.getAllBills().observe(this, Observer {
            if (it.isNotEmpty()) {
                listText.isVisible = false
                listImage.isVisible = false
                recycler_view.isVisible = true
                recycler_view.adapter = BillsRecyclerAdapter(it) { data: BillData -> itemClicked(data) }
            } else {
                listText.text = resources.getString(R.string.no_bills)
                listImage.setImageDrawable(IconicsDrawable(requireContext())
                        .icon(GoogleMaterial.Icon.gmd_insert_emoticon).sizeDp(24))
                listText.isVisible = true
                listImage.isVisible = true
                recycler_view.isVisible = false
            }
        })
        billViewModel.isLoading.observe(this, Observer {
            swipeContainer.isRefreshing = it == true
        })
        billViewModel.apiResponse.observe(this, Observer {
           toastError(it)
        })
    }

    private fun itemClicked(billData: BillData){
        val billDetail = Intent(requireActivity(), BillDetailActivity::class.java).apply {
            putExtras(bundleOf("billId" to billData.billId))
        }
        startActivity(billDetail)
    }

    private fun initFab(){
        fab.apply {
            translationY = (6 * 56).toFloat()
            animate().translationY(0.toFloat())
                    .setInterpolator(OvershootInterpolator(1f))
                    .setStartDelay(300)
                    .setDuration(400)
                    .start()

            setOnClickListener{
                fab.isClickable = false
                val addBill = AddBillDialog()
                addBill.arguments = bundleOf("revealX" to fab.width / 2, "revealY" to fab.height / 2)
                addBill.show(requireFragmentManager().beginTransaction(), "add_bill_dialog")
                fab.isClickable = true
            }
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
        activity?.activity_toolbar?.title = "Bills"
        fab.isVisible = true
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = "Bills"
    }

    override fun onStop() {
        super.onStop()
        fab.isGone = true
    }

    override fun onDetach() {
        RetrofitBuilder.destroyInstance()
        fab.isGone = true
        super.onDetach()
    }
}