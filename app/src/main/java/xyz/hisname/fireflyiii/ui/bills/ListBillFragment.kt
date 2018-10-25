package xyz.hisname.fireflyiii.ui.bills

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_bill.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.viewmodel.BillsViewModel
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
        return inflater.create(R.layout.fragment_bill, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        billDatabase = AppDatabase.getInstance(requireContext())
        displayView()
        pullToRefresh()
        initFab()
    }

    private fun displayView(){
        val viewModel = billViewModel.getBill(baseUrl,accessToken)
        swipeContainer.isRefreshing = true
        runLayoutAnimation(recycler_view)
        viewModel.apiResponse.observe(this, Observer {
            if(it.getErrorMessage() != null){
                toastError(it.getErrorMessage().toString())
            }
        })

        viewModel.databaseData?.observe(this, Observer {
            swipeContainer.isRefreshing = false
            if(it.isNotEmpty()) {
                happyFaceText.isVisible = false
                happyFace.isVisible = false
                recycler_view.isVisible = true
                recycler_view.adapter = BillsRecyclerAdapter(it) { data: BillData -> itemClicked(data) }
            } else {
                happyFaceText.isVisible = true
                happyFace.isVisible = true
                recycler_view.isVisible = false
            }
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
                    .setInterpolator(OvershootInterpolator(1.toFloat()))
                    .setStartDelay(300)
                    .setDuration(400)
                    .start()

            setOnClickListener{
                val addBillActivity = Intent(requireContext(), AddBillActivity::class.java)
                startActivity(addBillActivity)
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
        dataAdapter.clear()
        swipeContainer.setOnRefreshListener {
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
        AppDatabase.destroyInstance()
        RetrofitBuilder.destroyInstance()
        fab.isGone = true
        super.onDetach()
    }
}