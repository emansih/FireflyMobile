package xyz.hisname.fireflyiii.ui.bills

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_bill.*
import kotlinx.coroutines.experimental.launch
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.BillsViewModel
import xyz.hisname.fireflyiii.repository.viewmodel.room.DaoBillsViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastInfo

class ListBillFragment: BaseFragment() {

    private val model: BillsViewModel by lazy { getViewModel(BillsViewModel::class.java) }
    private lateinit var billsAdapter: BillsRecyclerAdapter
    private var dataAdapter = ArrayList<BillData>()
    private var billDatabase: AppDatabase? = null
    private val billsVM: DaoBillsViewModel by lazy { getViewModel(DaoBillsViewModel::class.java) }

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
        swipeContainer.isRefreshing = true
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        model.getBill(baseUrl, accessToken).observe(this, Observer {
            if(it.getError() == null){
                dataAdapter = ArrayList(it.getBill()?.data)
                happyFaceText.isInvisible = true
                happyFace.isInvisible = true
                recycler_view.isVisible = true
                addBillButton.isVisible = true
                if(billsAdapter.itemCount == 0){
                    recycler_view.isVisible = false
                    happyFaceText.isInvisible = false
                    happyFace.isInvisible = false
                } else {
                    showData(dataAdapter)
                    it.getBill()!!.data.forEachIndexed { _, element ->
                        launch {
                            billDatabase?.billDataDao()?.addBill(element)
                        }
                    }
                }
            } else {
                billsVM.getAllBills().observe(this, Observer { billData ->
                    if(billData.isEmpty() || billData == null){
                        if (it.getError()!!.localizedMessage.startsWith("Unable to resolve host")) {
                            recycler_view.isVisible = false
                            happyFace.apply {
                                isVisible = false
                                setImageDrawable(ContextCompat.getDrawable(requireContext(),
                                        R.drawable.ic_cloud_off))
                            }
                            happyFaceText.apply {
                                isInvisible = false
                                text = resources.getString(R.string.unable_ping_server)
                            }
                            addBillButton.isVisible = false
                            toastInfo("Please try again later")
                        }
                    } else {
                        showData(billData)
                        toastInfo("Loaded data from cache")
                    }

                })
            }
            swipeContainer.isRefreshing = false
        })
    }

    private fun showData(billData: MutableList<BillData>){
        billsAdapter = BillsRecyclerAdapter(dataAdapter) { billData: BillData -> itemClicked(billData)}
        recycler_view.adapter = billsAdapter
        billsAdapter.apply {
            recycler_view.adapter as BillsRecyclerAdapter
            update(billData)
        }
    }

    private fun itemClicked(billData: BillData){
        val bundle = bundleOf("fireflyUrl" to baseUrl, "access_token" to accessToken,
                "billId" to billData.billId, "billName" to billData.billAttributes?.name,
                "currencyCode" to billData.billAttributes?.currency_code, "billMax" to billData.billAttributes?.amount_max,
                "billMin" to billData.billAttributes?.amount_min, "date" to billData.billAttributes?.date)
        requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BillDetailFragment().apply { arguments = bundle })
                .addToBackStack(null)
                .commit()
    }

    private fun initFab(){
        addBillButton.apply {
            translationY = (6 * 56).toFloat()
            animate().translationY(0.toFloat())
                    .setInterpolator(OvershootInterpolator(1.toFloat()))
                    .setStartDelay(300)
                    .setDuration(400)
                    .start()
            setOnClickListener{
                val bundle = bundleOf("fireflyUrl" to baseUrl, "access_token" to accessToken)
                requireActivity().supportFragmentManager.beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.fragment_container, AddBillDialog().apply { arguments = bundle })
                        .addToBackStack(null)
                        .commit()
            }
        }
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                 if(dy > 0 && addBillButton.isShown){
                     addBillButton.hide()
                 }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    addBillButton.show()
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
        requireActivity().activity_toolbar.title = "Bills"
    }

    override fun onResume() {
        super.onResume()
        requireActivity().activity_toolbar.title = "Bills"
    }

    override fun onDetach() {
        AppDatabase.destroyInstance()
        RetrofitBuilder.destroyInstance()
        super.onDetach()
    }
}