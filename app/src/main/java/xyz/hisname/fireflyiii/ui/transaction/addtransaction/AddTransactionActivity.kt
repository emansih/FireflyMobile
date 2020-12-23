package xyz.hisname.fireflyiii.ui.transaction.addtransaction

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import kotlinx.android.synthetic.main.activity_add_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.util.extension.hideKeyboard

class AddTransactionActivity: BaseActivity() {

    private val transactionType by lazy { intent.getStringExtra("transactionType") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        hideKeyboard()
        setBottomNav()
        when (transactionType) {
            "Withdrawal" -> transactionBottomView.selectedItemId = R.id.action_withdraw
            "Deposit" -> transactionBottomView.selectedItemId = R.id.action_deposit
            "Transfer" -> transactionBottomView.selectedItemId = R.id.action_transfer
            else -> transactionBottomView.selectedItemId = R.id.action_withdraw
        }
    }

    private fun setBottomNav(){
        transactionBottomView.setOnNavigationItemSelectedListener{ item ->
            when(item.itemId){
                R.id.action_withdraw -> {
                    supportFragmentManager.commit {
                        replace(R.id.addTransactionFrame, AddTransactionPager().apply {
                            arguments = bundleOf("transactionType" to "Withdrawal", "FROM_TRANSACTION_ACTIVITY" to true)
                        })
                    }
                    true
                }
                R.id.action_deposit -> {
                    supportFragmentManager.commit {
                        replace(R.id.addTransactionFrame, AddTransactionPager().apply {
                            arguments = bundleOf("transactionType" to "Deposit", "FROM_TRANSACTION_ACTIVITY" to true)
                        })
                    }
                    true
                }
                R.id.action_transfer -> {
                    supportFragmentManager.commit {
                        replace(R.id.addTransactionFrame, AddTransactionPager().apply {
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