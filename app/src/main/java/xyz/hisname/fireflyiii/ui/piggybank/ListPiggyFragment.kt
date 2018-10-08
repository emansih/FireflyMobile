package xyz.hisname.fireflyiii.ui.piggybank

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.PiggyViewModel
import xyz.hisname.fireflyiii.repository.viewmodel.room.DaoPiggyViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastInfo

class ListPiggyFragment: BaseFragment() {

    private var dataAdapter = ArrayList<PiggyData>()
    private val model: PiggyViewModel by lazy { getViewModel(PiggyViewModel::class.java)}
    private val piggyVM: DaoPiggyViewModel by lazy { getViewModel(DaoPiggyViewModel::class.java) }
    private var piggyDataBase: AppDatabase? = null
    private val fab by lazy { requireActivity().findViewById<FloatingActionButton>(R.id.globalFAB) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.base_swipe_layout, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        piggyDataBase = AppDatabase.getInstance(requireContext())
        displayView()
        pullToRefresh()
        initFab()
    }

    private fun displayView(){
        swipeContainer.isRefreshing = true
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        model.getPiggyBanks(baseUrl, accessToken).observe(this, Observer {
            if(it.getError() == null){
                recycler_view.adapter = PiggyRecyclerAdapter(it.getPiggy()!!.data.toMutableList()) {
                    data: PiggyData -> itemClicked(data) }
                it.getPiggy()!!.data.forEachIndexed { _, element ->
                    GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT, null, {
                        piggyDataBase?.piggyDataDao()?.addPiggy(element)
                    })
                }
            } else {
                piggyVM.getPiggyBank().observe(this, Observer { piggyData ->
                    recycler_view.adapter = PiggyRecyclerAdapter(piggyData) { data: PiggyData -> itemClicked(data) }
                    toastInfo("Loaded data from cache")
                })

            }
            swipeContainer.isRefreshing = false
        })
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
        fab.apply {
            isVisible = true
            translationY = (6 * 56).toFloat()
            animate().translationY(0.toFloat())
                    .setInterpolator(OvershootInterpolator(1.toFloat()))
                    .setStartDelay(300)
                    .setDuration(400)
                    .start()
            setOnClickListener{
                startActivity(Intent(requireContext(), AddPiggyActivity::class.java))
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

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = "Piggy Bank"
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = "Piggy Bank"
    }

    override fun onStop() {
        super.onStop()
        fab.isGone = true
    }

    override fun onDetach() {
        super.onDetach()
        RetrofitBuilder.destroyInstance()
        AppDatabase.destroyInstance()
        fab.isGone = true
    }
}