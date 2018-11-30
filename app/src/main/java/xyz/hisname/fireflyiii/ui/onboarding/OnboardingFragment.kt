package xyz.hisname.fireflyiii.ui.onboarding

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import kotlinx.android.synthetic.main.fragment_onboarding.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.account.AccountsViewModel
import xyz.hisname.fireflyiii.repository.bills.BillsViewModel
import xyz.hisname.fireflyiii.repository.budget.BudgetViewModel
import xyz.hisname.fireflyiii.repository.category.CategoryViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyViewModel
import xyz.hisname.fireflyiii.repository.piggybank.PiggyViewModel
import xyz.hisname.fireflyiii.repository.transaction.TransactionsViewModel
import xyz.hisname.fireflyiii.repository.userinfo.UserInfoViewModel
import xyz.hisname.fireflyiii.ui.HomeActivity
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.workers.RefreshTokenWorker
import java.util.concurrent.TimeUnit

class OnboardingFragment: Fragment() {

    private val userInfoViewModel by lazy { getViewModel(UserInfoViewModel::class.java) }
    private val transaction by lazy { getViewModel(TransactionsViewModel::class.java) }
    private val currency by lazy { getViewModel(CurrencyViewModel::class.java) }
    private val accountViewModel by lazy { getViewModel(AccountsViewModel::class.java) }
    private val categoryViewModel by lazy { getViewModel(CategoryViewModel::class.java) }
    private val piggyViewModel by lazy { getViewModel(PiggyViewModel::class.java) }
    private val billViewModel by lazy { getViewModel(BillsViewModel::class.java) }
    private val budgetViewModel by lazy { getViewModel(BudgetViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_onboarding, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setRefreshWorker()
        getUser()
    }

    private fun getUser(){
        ObjectAnimator.ofInt(onboarding_progress,"progress", 10).start()
        RetrofitBuilder.destroyInstance()
        ObjectAnimator.ofInt(onboarding_progress,"progress", 20).start()
        zipLiveData(transaction.getAllData(DateTimeUtil.getStartOfMonth(6),
                DateTimeUtil.getTodayDate()), piggyViewModel.getAllPiggyBanks(), billViewModel.getAllBills(),
                currency.getCurrency(), categoryViewModel.getAllCategory(),
                accountViewModel.getAllAccounts())
        ObjectAnimator.ofInt(onboarding_progress,"progress", 50).start()
        accountViewModel.getAllAccounts().observe(this, Observer {
            ObjectAnimator.ofInt(onboarding_progress,"progress", 60).start()
            onboarding_text.text = "Just hang in there..."
        })
        accountViewModel.getAllAccounts().observe(this, Observer {
            ObjectAnimator.ofInt(onboarding_progress,"progress", 70).start()
        })
        budgetViewModel.retrieveAllBudgetLimits().observe(this, Observer {
            ObjectAnimator.ofInt(onboarding_progress,"progress", 80).start()
            onboarding_text.text = "Almost there!"
        })
        budgetViewModel.retrieveAllBudget().observe(this, Observer {
            ObjectAnimator.ofInt(onboarding_progress,"progress", 90).start()
        })
        zipLiveData(userInfoViewModel.getUser(),userInfoViewModel.userSystem()).observe(this, Observer {
            startActivity(Intent(requireActivity(), HomeActivity::class.java))
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            requireActivity().finish()
        })
    }

    private fun setRefreshWorker(){
        val workBuilder = PeriodicWorkRequest
                .Builder(RefreshTokenWorker::class.java, 24, TimeUnit.HOURS)
                .addTag("refresh_worker")
                .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build())
                .build()
        WorkManager.getInstance().enqueue(workBuilder)
        ObjectAnimator.ofInt(onboarding_progress,"progress", 40).start()
    }

}