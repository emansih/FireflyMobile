package xyz.hisname.fireflyiii.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.transaction.RecentTransactionFragment
import xyz.hisname.fireflyiii.util.DeviceUtil
import xyz.hisname.fireflyiii.util.extension.create

class DashboardFragment: BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_dashboard,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeContainer.isRefreshing = true
        animateCard(overviewFrame,walletFrame,budgetFrame, recentTransactionFrame)
        swipeContainer.isRefreshing = false
        setRefreshing()
    }

    private fun animateCard(vararg frameLayout: FrameLayout){
        for(frames in frameLayout){
            frames.translationY = DeviceUtil.getScreenHeight(requireContext()).toFloat()
            frames.animate()
                    .translationY(0f)
                    .setInterpolator(DecelerateInterpolator(5f))
                    .setDuration(3000)
                    .start()
        }
        setUpCards()
    }

    private fun setUpCards(){
        val bundle = bundleOf("transactionType" to "all")
        requireFragmentManager().beginTransaction()
                .replace(R.id.overviewFrame, OverviewFragment().apply { arguments = bundle })
                .commit()
        requireFragmentManager().beginTransaction()
                .replace(R.id.walletFrame, WalletFragment())
                .commit()
        requireFragmentManager().beginTransaction()
                .replace(R.id.budgetFrame, BudgetFragment())
                .commit()
        requireFragmentManager().beginTransaction()
                .replace(R.id.recentTransactionFrame, RecentTransactionFragment().apply { arguments = bundle })
                .commit()
    }

    private fun setRefreshing(){
        swipeContainer.setOnRefreshListener {
            swipeContainer.isRefreshing = true
            requireFragmentManager().beginTransaction().remove(OverviewFragment()).commit()
            requireFragmentManager().beginTransaction().remove(WalletFragment()).commit()
            requireFragmentManager().beginTransaction().remove(BudgetFragment()).commit()
            requireFragmentManager().beginTransaction().remove(RecentTransactionFragment()).commit()
            animateCard(overviewFrame,walletFrame,budgetFrame,recentTransactionFrame)
            swipeContainer.isRefreshing = false
        }
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.dashboard)
    }

}