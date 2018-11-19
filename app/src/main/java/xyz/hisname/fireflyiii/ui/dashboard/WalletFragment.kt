package xyz.hisname.fireflyiii.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_dashboard_wallet.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.account.AccountsViewModel
import xyz.hisname.fireflyiii.ui.account.ListAccountFragment
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel

class WalletFragment: BaseFragment() {

    private val bundle: Bundle by lazy { bundleOf("accountType" to "all") }
    private val accountsRepo by lazy { getViewModel(AccountsViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_dashboard_wallet,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayData()
        overviewCard.setOnClickListener {
            requireFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ListAccountFragment().apply { arguments = bundle }, "wallet")
                    .addToBackStack(null)
                    .commit()
        }
    }

    private fun displayData() {
        accountsRepo.getTotalCashAccount().observe(this, Observer {
            cashText.text = it
        })
        accountsRepo.getTotalAssetAccount().observe(this, Observer {
            assetsText.text = it
        })
        accountsRepo.getTotalLoanAccount().observe(this, Observer {
            loanText.text = it
        })

    }
}