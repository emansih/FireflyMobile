package xyz.hisname.fireflyiii.ui.tasker

import android.os.Bundle
import androidx.core.view.isGone
import androidx.lifecycle.observe
import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractAppCompatPluginActivity
import kotlinx.android.synthetic.main.activity_add_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.extension.getViewModel

abstract class BaseTransactionPlugin: AbstractAppCompatPluginActivity() {

    protected val bundle by lazy { Bundle() }
    protected val resultBlurb by lazy { StringBuilder().append("The following will be ran: ") }
    protected val viewModel by lazy { getViewModel(TransactionPluginViewModel::class.java) }

    abstract fun navigateFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        transactionBottomView.isGone = true
        observeText()
        navigateFragment()
    }

    private fun observeText(){
        viewModel.transactionType.observe(this){ type ->
            if (type.isNotBlank()){
                resultBlurb.append("\n Transaction Type: $type")
                bundle.putString("transactionType", type)
            }
        }

        viewModel.transactionDescription.observe(this){ description ->
            if (description.isNotBlank()){
                resultBlurb.append("\n Transaction description: $description")
                bundle.putString("transactionDescription", description)
            }
        }

        viewModel.transactionAmount.observe(this){ amount ->
            if (amount.isNotBlank()){
                resultBlurb.append("\n Amount: $amount")
                bundle.putString("transactionAmount", amount)
            }
        }

        viewModel.transactionDateTime.observe(this){ dateTime ->
            if (dateTime.isNotBlank()){
                resultBlurb.append("\n Date Time: $dateTime")
                bundle.putString("transactionDateTime", dateTime)
            }
        }

        viewModel.transactionPiggyBank.observe(this){ piggyBank ->
            if (piggyBank != null && piggyBank.isNotBlank()){
                resultBlurb.append("\n Piggy Bank: $piggyBank")
                bundle.putString("transactionPiggyBank", piggyBank)
            }
        }

        viewModel.transactionSourceAccount.observe(this){ sourceAccount ->
            if (sourceAccount.isNotBlank()){
                resultBlurb.append("\n Source Account: $sourceAccount")
                bundle.putString("transactionSourceAccount", sourceAccount)
            }
        }

        viewModel.transactionDestinationAccount.observe(this){ destinationAccount ->
            if (destinationAccount.isNotBlank()){
                resultBlurb.append("\n Destination Account: $destinationAccount")
                bundle.putString("transactionDestinationAccount", destinationAccount)
            }
        }

        viewModel.transactionCurrency.observe(this){ currency ->
            if (currency.isNotBlank()){
                resultBlurb.append("\n Currency Code: $currency")
                bundle.putString("transactionCurrency", currency)
            }
        }

        viewModel.transactionTags.observe(this){ tags ->
            if (tags != null && tags.isNotBlank()){
                resultBlurb.append("\n Tags: $tags")
                bundle.putString("transactionTags", tags)
            }
        }

        viewModel.transactionBudget.observe(this){ budget ->
            if (budget != null && budget.isNotBlank()){
                resultBlurb.append("\n Budget: $budget")
                bundle.putString("transactionBudget", budget)
            }
        }

        viewModel.transactionCategory.observe(this){ category ->
            if (category != null && category.isNotBlank()){
                resultBlurb.append("\n Category: $category")
                bundle.putString("transactionCategory", category)
            }
        }

        viewModel.fileUri.observe(this){ uri ->
            if(uri != null){
                resultBlurb.append("\n File to be uploaded: $uri")
                bundle.putString("fileUri", uri.toString())
            }
        }

        viewModel.removeFragment.observe(this){ shouldRemove ->
            if(shouldRemove){
                onBackPressed()
            }
        }
    }

    override fun onPostCreateWithPreviousResult(bundle: Bundle, string: String) {
        viewModel.transactionBundle.postValue(bundle)
    }

    override fun getResultBlurb(bundle: Bundle): String {
        return resultBlurb.toString()
    }


    override fun getResultBundle() = bundle

    override fun isBundleValid(bundle: Bundle) =  true

}