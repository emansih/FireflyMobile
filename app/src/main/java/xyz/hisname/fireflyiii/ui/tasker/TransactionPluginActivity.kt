package xyz.hisname.fireflyiii.ui.tasker

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractAppCompatPluginActivity
import kotlinx.android.synthetic.main.activity_add_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionFragment
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.hideKeyboard

class TransactionPluginActivity: AbstractAppCompatPluginActivity() {

    private val bundle by lazy { Bundle() }
    private val resultBlurb by lazy { StringBuilder().append("The following will be ran: ") }
    private val viewModel by lazy { getViewModel(TransactionPluginViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        hideKeyboard()
        setBottomNav()
        observeText()
        when (bundle.getString("transactionType")) {
            "Withdrawal" -> transactionBottomView.selectedItemId = R.id.action_withdraw
            "Deposit" -> transactionBottomView.selectedItemId = R.id.action_deposit
            "Transfer" -> transactionBottomView.selectedItemId = R.id.action_transfer
            else -> transactionBottomView.selectedItemId = R.id.action_withdraw
        }
    }

    private fun observeText(){
        viewModel.transactionType.observe(this){ type ->
            if (type.isNotBlank()){
                resultBlurb.append("\n Transaction Type: $type")
            }
        }

        viewModel.transactionDescription.observe(this){ description ->
            if (description.isNotBlank()){
                resultBlurb.append("\n Transaction description: $description")
            }
        }

        viewModel.transactionDateTime.observe(this){ dateTime ->
            if (dateTime.isNotBlank()){
                resultBlurb.append("\n Date Time: $dateTime")
            }
        }

        viewModel.transactionPiggyBank.observe(this){ piggyBank ->
            if (piggyBank != null && piggyBank.isNotBlank()){
                resultBlurb.append("\n Piggy Bank: $piggyBank")
            }
        }

        viewModel.transactionSourceAccount.observe(this){ sourceAccount ->
            if (sourceAccount.isNotBlank()){
                resultBlurb.append("\n Source Account: $sourceAccount")
            }
        }

        viewModel.transactionDestinationAccount.observe(this){ destinationAccount ->
            if (destinationAccount.isNotBlank()){
                resultBlurb.append("\n Destination Account: $destinationAccount")
            }
        }

        viewModel.transactionCurrency.observe(this){ currency ->
            if (currency.isNotBlank()){
                resultBlurb.append("\n Currency Code: $currency")
            }
        }

        viewModel.transactionTags.observe(this){ tags ->
            if (tags != null && tags.isNotBlank()){
                resultBlurb.append("\n Tags: $tags")
            }
        }

        viewModel.transactionBudget.observe(this){ budget ->
            if (budget != null && budget.isNotBlank()){
                resultBlurb.append("\n Budget: $budget")
            }
        }

        viewModel.transactionCategory.observe(this){ category ->
            if (category != null && category.isNotBlank()){
                resultBlurb.append("\n Category: $category")
            }
        }

        viewModel.fileUri.observe(this){ uri ->
            if(uri != null){
                resultBlurb.append("\n File to be uploaded: $uri")
            }
        }
    }

    private fun setBottomNav(){
        val nightMode = AppPref(PreferenceManager.getDefaultSharedPreferences(this)).nightModeEnabled
        if(nightMode) {
            transactionBottomView.itemTextColor = ContextCompat.getColorStateList(this, R.color.md_white_1000)
            transactionBottomView.itemIconTintList = ContextCompat.getColorStateList(this, R.color.md_white_1000)
        }
        val addTransactionFragment = AddTransactionFragment()
        transactionBottomView.setOnNavigationItemSelectedListener{ item ->
            when(item.itemId){
                R.id.action_withdraw -> {
                    supportFragmentManager.commit {
                        replace(R.id.addTransactionFrame, addTransactionFragment.apply {
                            arguments = bundleOf("transactionType" to "Withdrawal", "isTasker" to true)
                        })
                    }
                    true
                }
                R.id.action_deposit -> {
                    supportFragmentManager.commit {
                        replace(R.id.addTransactionFrame, addTransactionFragment.apply {
                            arguments = bundleOf("transactionType" to "Deposit", "isTasker" to true)
                        })
                    }
                    true
                }
                R.id.action_transfer -> {
                    supportFragmentManager.commit {
                        replace(R.id.addTransactionFrame, addTransactionFragment.apply {
                            arguments = bundleOf("transactionType" to "Transfer", "isTasker" to true)
                        })
                    }
                    true
                } else -> {
                true
            }
            }
        }
        viewModel.removeFragment.observe(this){ shouldRemove ->
            if(shouldRemove){
                onBackPressed()
            }
        }
    }

    override fun onPostCreateWithPreviousResult(bundle: Bundle, string: String) {
    }

    override fun getResultBlurb(bundle: Bundle): String {
        return resultBlurb.toString()
    }


    override fun getResultBundle() = bundle

    override fun isBundleValid(bundle: Bundle) =  true

}