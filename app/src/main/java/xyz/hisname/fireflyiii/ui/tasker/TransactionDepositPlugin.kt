package xyz.hisname.fireflyiii.ui.tasker

import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionPager


class TransactionDepositPlugin: TransactionConfigPlugin<GetTransactionInput,
        GetTransactionOutput, GetTransactionRunner, GetTransactionHelper>() {

    override fun navigateFragment() {
        supportFragmentManager.commit {
            replace(R.id.addTransactionFrame, AddTransactionPager().apply {
                arguments = bundleOf("transactionType" to "Deposit", "isTasker" to true)
            })
        }
    }
}