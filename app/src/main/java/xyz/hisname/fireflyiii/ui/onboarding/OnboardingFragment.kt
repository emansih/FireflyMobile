package xyz.hisname.fireflyiii.ui.onboarding

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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
        getUser()
    }

    private fun getUser(){
        ObjectAnimator.ofInt(onboarding_progress,"progress", 10).start()
        RetrofitBuilder.destroyInstance()
        ObjectAnimator.ofInt(onboarding_progress,"progress", 20).start()
        transaction.getAllData(DateTimeUtil.getStartOfMonth(6), DateTimeUtil.getEndOfMonth()).observe(this, Observer {
            ObjectAnimator.ofInt(onboarding_progress,"progress", 30).start()
        })
        piggyViewModel.getAllPiggyBanks().observe(this, Observer {
            ObjectAnimator.ofInt(onboarding_progress,"progress", 60).start()
        })
        billViewModel.getAllBills()
        budgetViewModel.retrieveAllBudgetLimits().observe(this, Observer {
            onboarding_text.text = "Just hang in there..."
            ObjectAnimator.ofInt(onboarding_progress,"progress", 70).start()
        })
        categoryViewModel.getAllCategory()
        accountViewModel.getAllAccounts().observe(this, Observer {
            ObjectAnimator.ofInt(onboarding_progress,"progress", 80).start()
        })
        currency.getCurrency()
        userInfoViewModel.getUser().observe(this, Observer {
            onboarding_text.text = "Almost there!"
            ObjectAnimator.ofInt(onboarding_progress,"progress", 95).start()
        })
        userInfoViewModel.userSystem().observe(this, Observer {
            startActivity(Intent(requireActivity(), HomeActivity::class.java))
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            requireActivity().finish()
        })
    }


}