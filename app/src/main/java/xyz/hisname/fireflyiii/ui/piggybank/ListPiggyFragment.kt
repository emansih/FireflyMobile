package xyz.hisname.fireflyiii.ui.piggybank

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_piggy_list.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.dao.DbWorker
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.PiggyViewModel
import xyz.hisname.fireflyiii.repository.viewmodel.room.DaoPiggyViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastInfo

class ListPiggyFragment: BaseFragment() {

    private lateinit var piggyAdapter: PiggyRecyclerAdapter
    private var dataAdapter = ArrayList<PiggyData>()
    private val model: PiggyViewModel by lazy { getViewModel(PiggyViewModel::class.java)}
    private val piggyVM: DaoPiggyViewModel by lazy { getViewModel(DaoPiggyViewModel::class.java) }
    private var piggyDataBase: AppDatabase? = null
    private lateinit var dbWorker: DbWorker

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_piggy_list, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        piggyDataBase = AppDatabase.getInstance(requireContext())
        dbWorker = DbWorker("piggyWorkerThread")
        dbWorker.start()
        displayView()
        pullToRefresh()
        initFab()
    }

    private fun displayView(){
        swipeContainer.isRefreshing = true
        recycler_view.layoutManager = LinearLayoutManager(requireContext())

        model.getPiggyBanks(baseUrl, accessToken).observe(this, Observer {
            if(it.getError() == null){
                dataAdapter = ArrayList(it.getPiggy()?.data)
                piggyAdapter = PiggyRecyclerAdapter(dataAdapter){ piggyData: PiggyData -> itemClicked(piggyData)}
                showData(it.getPiggy()!!.data.toMutableList())
                it.getPiggy()!!.data.forEachIndexed { _, element ->
                    insertDataIntoDb(element)
                }
            } else {
                piggyVM.getPiggyBank().observe(this, Observer { piggyData ->
                    piggyAdapter = PiggyRecyclerAdapter(piggyData) { data: PiggyData -> itemClicked(data)}
                    showData(piggyData)
                    toastInfo("Loaded data from cache")
                })

            }
            swipeContainer.isRefreshing = false
        })
    }

    private fun showData(piggyData: MutableList<PiggyData>){
        recycler_view.adapter = piggyAdapter
        piggyAdapter = recycler_view.adapter as PiggyRecyclerAdapter
        piggyAdapter.update(piggyData)
    }

    private fun insertDataIntoDb(piggyData: PiggyData){
        val task = Runnable {
            piggyDataBase?.piggyDataDao()?.addPiggy(piggyData)
        }
        dbWorker.postTask(task)
    }

    private fun itemClicked(piggyData: PiggyData){
        val bundle = bundleOf("fireflyUrl" to baseUrl, "access_token" to accessToken,
                "percentage" to piggyData.piggyAttributes?.percentage, "currentAmount" to piggyData.piggyAttributes?.current_amount,
                "leftToSave" to piggyData.piggyAttributes?.left_to_save, "piggyId" to piggyData.piggyId,
                "currencyCode" to piggyData.piggyAttributes?.currency_code, "targetAmount" to piggyData.piggyAttributes?.target_amount,
                "name" to piggyData.piggyAttributes?.name, "targetDate" to piggyData.piggyAttributes?.target_date,
                "savePerMonth" to piggyData.piggyAttributes?.save_per_month)
        requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PiggyDetailFragment().apply { arguments = bundle })
                .addToBackStack(null)
                .commit()

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

    private fun initFab(){
        addPiggyButton.translationY = (6 * 56).toFloat()
        addPiggyButton.animate()
                .translationY(0.toFloat())
                .setInterpolator(OvershootInterpolator(1.toFloat()))
                .setStartDelay(300)
                .setDuration(400)
                .start()
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dy > 0 && addPiggyButton.isShown){
                    addPiggyButton.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    addPiggyButton.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = "Piggy Bank"
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = "Piggy Bank"
    }

    override fun onDetach() {
        super.onDetach()
        RetrofitBuilder.destroyInstance()
        dbWorker.quit()
        AppDatabase.destroyInstance()

    }
}