package xyz.hisname.fireflyiii.ui.transaction.addtransaction

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import kotlinx.android.synthetic.main.activity_add_transaction.*
import me.toptas.fancyshowcase.FancyShowCaseView
import me.toptas.fancyshowcase.FocusShape
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.util.extension.hideKeyboard
import xyz.hisname.fireflyiii.util.extension.onAnimationEnd

class AddTransactionActivity: BaseActivity() {

    private val transactionType by lazy { intent.getStringExtra("transactionType") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        hideKeyboard()
        setBottomNav()
        setHelpText()
        when (transactionType) {
            "Withdrawal" -> transactionBottomView.selectedItemId = R.id.action_withdraw
            "Deposit" -> transactionBottomView.selectedItemId = R.id.action_deposit
            "Transfer" -> transactionBottomView.selectedItemId = R.id.action_transfer
            else -> transactionBottomView.selectedItemId = R.id.action_withdraw
        }
    }

    private fun setHelpText(){
        val enterAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_from_left)
        val exitAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_to_right)
        val navCaseView = FancyShowCaseView.Builder(this)
                .focusOn(transactionBottomView)
                .title(resources.getString(R.string.transactions_create_switch_box))
                .enableAutoTextPosition()
                .showOnce("bottomNavigationShowCase")
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .closeOnTouch(true)
                .enterAnimation(enterAnimation)
                .exitAnimation(exitAnimation)
                .build()
        navCaseView.show()
        exitAnimation.onAnimationEnd {
            navCaseView.removeView()
        }
    }

    private fun setBottomNav(){
        transactionBottomView.setOnNavigationItemSelectedListener{ item ->
            when(item.itemId){
                R.id.action_withdraw -> {
                    supportFragmentManager.commit {
                        replace(R.id.addTransactionFrame, AddTransactionFragment().apply {
                            arguments = bundleOf("transactionType" to "Withdrawal")
                        })
                    }
                    true
                }
                R.id.action_deposit -> {
                    supportFragmentManager.commit {
                        replace(R.id.addTransactionFrame, AddTransactionFragment().apply {
                            arguments = bundleOf("transactionType" to "Deposit")
                        })
                    }
                    true
                }
                R.id.action_transfer -> {
                    supportFragmentManager.commit {
                        replace(R.id.addTransactionFrame, AddTransactionFragment().apply {
                            arguments = bundleOf("transactionType" to "Transfer")
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