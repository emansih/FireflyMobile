package xyz.hisname.fireflyiii.ui.tasker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfos
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginRunner
import kotlinx.android.synthetic.main.activity_add_transaction.*
import net.dinglisch.android.tasker.TaskerPlugin
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.extension.getViewModel


abstract class TransactionConfigPlugin<TInput : Any, TOutput : Any,
        TActionRunner : TaskerPluginRunner<TInput, TOutput>,
        THelper: TaskerPluginConfigHelper<TInput, TOutput, TActionRunner>>: TaskerPluginConfig<GetTransactionInput>, AppCompatActivity() {

    abstract fun navigateFragment()

    private val taskerHelper by lazy { getNewHelper(this) }
    private val viewModel by lazy { getViewModel(TransactionPluginViewModel::class.java) }
    private var transactionType: String? = null
    private var transactionDescription: String? = null
    private var transactionAmount: String? = null
    private var transactionDate: String? = null
    private var transactionCurrency: String? = null
    private var transactionTime: String? = null
    private var transactionPiggyBank: String? = null
    private var transactionSourceAccount: String? = null
    private var transactionDestinationAccount: String? = null
    private var transactionTags: String? = null
    private var transactionBudget: String? = null
    private var transactionCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        transactionBottomView.isGone = true
        navigateFragment()
        observeText()
    }

    private fun observeText(){
        viewModel.transactionBundle.postValue(bundleOf(
                "transactionDescription" to taskerHelper.config.getIntent()?.getStringExtra("transactionDescription"),
                "transactionAmount" to taskerHelper.config.getIntent()?.getStringExtra("transactionAmount"),
                "transactionCurrency" to taskerHelper.config.getIntent()?.getStringExtra("transactionCurrency"),
                "transactionDate" to taskerHelper.config.getIntent()?.getStringExtra("transactionDate"),
                "transactionSourceAccount" to taskerHelper.config.getIntent()?.getStringExtra("transactionSourceAccount"),
                "transactionDestinationAccount" to taskerHelper.config.getIntent()?.getStringExtra("transactionDestinationAccount"),
                "transactionTime" to taskerHelper.config.getIntent()?.getStringExtra("transactionTime"),
                "transactionCategory" to taskerHelper.config.getIntent()?.getStringExtra("transactionCategory"),
                "transactionTags" to taskerHelper.config.getIntent()?.getStringExtra("transactionTags"),
                "transactionBudget"  to taskerHelper.config.getIntent()?.getStringExtra("transactionBudget"),
                "transactionPiggyBank" to taskerHelper.config.getIntent()?.getStringExtra("transactionPiggyBank")
        ))
        viewModel.transactionType.observe(this) { type ->
            if (type.isNotBlank()) {
                transactionType = type
            }
        }

        viewModel.transactionDescription.observe(this) { description ->
            if (description.isNotBlank()) {
                transactionDescription = description
            }
        }

        viewModel.transactionAmount.observe(this) { amount ->
            if (amount.isNotBlank()) {
                transactionAmount = amount
            }
        }

        viewModel.transactionDate.observe(this) { date ->
            if (date.isNotBlank()) {
                transactionDate = date
            }
        }

        viewModel.transactionTime.observe(this) { time ->
            if (time.isNotBlank()) {
                transactionTime = time
            }
        }

        viewModel.transactionPiggyBank.observe(this) { piggyBank ->
            if (piggyBank != null && piggyBank.isNotBlank()) {
                transactionPiggyBank = piggyBank
            }
        }

        viewModel.transactionSourceAccount.observe(this) { sourceAccount ->
            if (sourceAccount.isNotBlank()) {
               transactionSourceAccount = sourceAccount
            }
        }

        viewModel.transactionDestinationAccount.observe(this) { destinationAccount ->
            if (destinationAccount.isNotBlank()) {
                transactionDestinationAccount = destinationAccount
            }
        }

        viewModel.transactionCurrency.observe(this) { currency ->
            if (currency.isNotBlank()) {
                transactionCurrency = currency
            }
        }

        viewModel.transactionTags.observe(this) { tags ->
            if (tags != null && tags.isNotBlank()) {
                transactionTags = tags
            }
        }

        viewModel.transactionBudget.observe(this) { budget ->
            if (budget != null && budget.isNotBlank()) {
                transactionBudget = budget
            }
        }

        viewModel.transactionCategory.observe(this) { category ->
            if (category != null && category.isNotBlank()) {
                transactionCategory = category
            }
        }

        viewModel.removeFragment.observe(this){ remove ->
            if(remove) {
                taskerHelper.finishForTasker()
            }
        }
    }

    private fun getNewHelper(config: TaskerPluginConfig<GetTransactionInput>) = GetTransactionHelper(config)

    override val context get() = this

    override fun assignFromInput(input: TaskerInput<GetTransactionInput>) { }

   override val inputForTasker: TaskerInput<GetTransactionInput>
        get() = TaskerInput(GetTransactionInput(
                transactionType, transactionDescription, transactionAmount,
                transactionDate, transactionSourceAccount, transactionDestinationAccount,
                transactionPiggyBank, transactionTime, transactionCurrency, transactionTags,
                transactionBudget, transactionCategory))
}