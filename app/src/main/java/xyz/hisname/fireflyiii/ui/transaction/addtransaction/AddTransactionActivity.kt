package xyz.hisname.fireflyiii.ui.transaction.addtransaction

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import kotlinx.android.synthetic.main.activity_add_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.util.extension.hideKeyboard
import xyz.hisname.fireflyiii.util.extension.showCase

class AddTransactionActivity: BaseActivity() {

    private val transactionType by lazy { intent.getStringExtra("transactionType") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        hideKeyboard()
        setBottomNav()
        setHelpText()
        backstackCounter()
        when (transactionType) {
            "Withdrawal" -> transactionBottomView.selectedItemId = R.id.action_withdraw
            "Deposit" -> transactionBottomView.selectedItemId = R.id.action_deposit
            "Transfer" -> transactionBottomView.selectedItemId = R.id.action_transfer
            else -> transactionBottomView.selectedItemId = R.id.action_withdraw
        }
    }

    private fun backstackCounter(){
        supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.backStackEntryCount == 1){
                addTransaction_coordinatorLayout.isInvisible = true
            } else if(supportFragmentManager.backStackEntryCount == 0){
                addTransaction_coordinatorLayout.isVisible = true
            }
        }

    }

    private fun setHelpText(){
        showCase(R.string.transactions_create_switch_box, "bottomNavigationShowCase",
                transactionBottomView, false).show()
    }

    private fun setBottomNav(){
        if(globalViewModel.isDark) {
            transactionBottomView.itemTextColor = ContextCompat.getColorStateList(this, R.color.md_white_1000)
            transactionBottomView.itemIconTintList = ContextCompat.getColorStateList(this, R.color.md_white_1000)
        }
        transactionBottomView.setOnNavigationItemSelectedListener{ item ->
            when(item.itemId){
                R.id.action_withdraw -> {
                    supportFragmentManager.commit {
                        replace(R.id.addTransactionFrame, AddTransactionFragment().apply {
                            arguments = bundleOf("transactionType" to "Withdrawal", "FROM_TRANSACTION_ACTIVITY" to true)
                        })
                    }
                    true
                }
                R.id.action_deposit -> {
                    supportFragmentManager.commit {
                        replace(R.id.addTransactionFrame, AddTransactionFragment().apply {
                            arguments = bundleOf("transactionType" to "Deposit", "FROM_TRANSACTION_ACTIVITY" to true)
                        })
                    }
                    true
                }
                R.id.action_transfer -> {
                    supportFragmentManager.commit {
                        replace(R.id.addTransactionFrame, AddTransactionFragment().apply {
                            arguments = bundleOf("transactionType" to "Transfer", "FROM_TRANSACTION_ACTIVITY" to true)
                        })
                    }
                    true
                } else -> {
                true
            }
            }
        }
    }

}