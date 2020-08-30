package xyz.hisname.fireflyiii.ui.tasker

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.lifecycle.observe
import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractAppCompatPluginActivity
import kotlinx.android.synthetic.main.activity_add_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.TaskerPlugin
import xyz.hisname.fireflyiii.util.extension.getViewModel

abstract class BaseTransactionPlugin: AbstractAppCompatPluginActivity() {

    protected val bundle by lazy { Bundle() }
    private val resultBlurb by lazy { StringBuilder().append("The following will be ran: ") }
    private val viewModel by lazy { getViewModel(TransactionPluginViewModel::class.java) }

    abstract fun navigateFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        transactionBottomView.isGone = true
        observeText()
        navigateFragment()
    }

    private fun observeText() {
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
                super.onBackPressed()
            }
        }
    }

    override fun onPostCreateWithPreviousResult(bundle: Bundle, string: String) {
        viewModel.transactionBundle.postValue(bundle)
    }

    override fun getResultBlurb(bundle: Bundle): String {
        val transactionDescription = bundle.getString("transactionDescription")
        val transactionType = bundle.getString("transactionType")
        val transactionAmount = bundle.getString("transactionAmount")
        val transactionDateTime = bundle.getString("transactionDateTime")
        val transactionSourceAccount = bundle.getString("transactionSourceAccount")
        val transactionDestinationAccount = bundle.getString("transactionDestinationAccount")
        val transactionCurrency = bundle.getString("transactionCurrency")
        if (TaskerPlugin.Setting.hostSupportsOnFireVariableReplacement(this)){
            TaskerPlugin.Setting.setVariableReplaceKeys(resultBundle, arrayOf(
                    "transactionDescription", "transactionAmount", "transactionDateTime",
                    "transactionPiggyBank", "transactionSourceAccount",
                    "transactionDestinationAccount", "transactionCurrency", "transactionTags",
                    "transactionBudget", "transactionCategory", "fileUri"))
        }
        return if(transactionDescription == null || transactionType == null || transactionAmount == null ||
                transactionDateTime == null || transactionSourceAccount == null ||
                transactionDestinationAccount == null || transactionCurrency == null){
            resultBlurb.clear()
            resultBlurb.append("Invalid data. Task will not run")
            resultBlurb.toString()
        } else {
            resultBlurb.toString()
        }
    }


    override fun getResultBundle() = bundle

    // This is called when going to edit screen
    override fun isBundleValid(bundle: Bundle): Boolean {
        bundle.getString("transactionDescription") ?: return false
        bundle.getString("transactionType") ?: return false
        bundle.getString("transactionAmount") ?: return false
        bundle.getString("transactionDateTime") ?: return false
        bundle.getString("transactionSourceAccount") ?: return false
        bundle.getString("transactionDestinationAccount") ?: return false
        bundle.getString("transactionCurrency") ?: return false
        return true
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("Your data is not saved and this task will not run")
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(android.R.string.ok){ _, _ ->
                    super.onBackPressed()
                }
                .show()
    }

}