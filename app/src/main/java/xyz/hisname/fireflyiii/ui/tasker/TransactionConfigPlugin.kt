package xyz.hisname.fireflyiii.ui.tasker

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginRunner
import kotlinx.android.synthetic.main.activity_add_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionViewModel
import xyz.hisname.fireflyiii.util.extension.getViewModel


abstract class TransactionConfigPlugin<TInput : Any, TOutput : Any,
        TActionRunner : TaskerPluginRunner<TInput, TOutput>,
        THelper: TaskerPluginConfigHelper<TInput, TOutput, TActionRunner>>: TaskerPluginConfig<GetTransactionInput>, BaseActivity() {

    abstract fun navigateFragment()

    private val taskerHelper by lazy { getNewHelper(this) }
    private val addTransactionViewModel by lazy { getViewModel(AddTransactionViewModel::class.java) }
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
    private var transactionNote: String? = null
    private var transactionUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        transactionBottomView.isGone = true
        navigateFragment()
        observeText()
    }

    private fun observeText(){
        addTransactionViewModel.transactionBundle.postValue(bundleOf(
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
                "transactionPiggyBank" to taskerHelper.config.getIntent()?.getStringExtra("transactionPiggyBank"),
                "transactionNote" to taskerHelper.config.getIntent()?.getStringExtra("transactionNote"),
                "transactionUri" to taskerHelper.config.getIntent()?.getSerializableExtra("transactionUri")
        ))
        addTransactionViewModel.transactionType.observe(this) { type ->
            if (type != null && type.isNotBlank()) {
                transactionType = type
            }
        }

        addTransactionViewModel.transactionDescription.observe(this) { description ->
            if (description != null && description.isNotBlank()) {
                transactionDescription = description
            }
        }

        addTransactionViewModel.transactionAmount.observe(this) { amount ->
            if (amount != null && amount.isNotBlank()) {
                transactionAmount = amount
            }
        }

        addTransactionViewModel.transactionDate.observe(this) { date ->
            if (date != null && date.isNotBlank()) {
                transactionDate = date
            }
        }

        addTransactionViewModel.transactionTime.observe(this) { time ->
            if (time != null && time.isNotBlank()) {
                transactionTime = time
            }
        }

        addTransactionViewModel.transactionPiggyBank.observe(this) { piggyBank ->
            if (piggyBank != null && piggyBank.isNotBlank()) {
                transactionPiggyBank = piggyBank
            }
        }

        addTransactionViewModel.transactionSourceAccount.observe(this) { sourceAccount ->
            if (sourceAccount != null && sourceAccount.isNotBlank()) {
               transactionSourceAccount = sourceAccount
            }
        }

        addTransactionViewModel.transactionDestinationAccount.observe(this) { destinationAccount ->
            if (destinationAccount != null && destinationAccount.isNotBlank()) {
                transactionDestinationAccount = destinationAccount
            }
        }

        addTransactionViewModel.transactionCurrency.observe(this) { currency ->
            if (currency != null && currency.isNotBlank()) {
                transactionCurrency = currency
            }
        }

        addTransactionViewModel.transactionTags.observe(this) { tags ->
            if (tags != null && tags.isNotBlank()) {
                transactionTags = tags
            }
        }

        addTransactionViewModel.transactionBudget.observe(this) { budget ->
            if (budget != null && budget.isNotBlank()) {
                transactionBudget = budget
            }
        }

        addTransactionViewModel.transactionCategory.observe(this) { category ->
            if (category != null && category.isNotBlank()) {
                transactionCategory = category
            }
        }

        addTransactionViewModel.transactionNote.observe(this){ note ->
            if (note != null && note.isNotBlank()) {
                transactionNote = note
            }
        }

        addTransactionViewModel.fileUri.observe(this){ uriArray ->
            if(uriArray != null && uriArray.isNotEmpty()){
                val arrayOfString = arrayListOf<String>()
                uriArray.forEach {  uri ->
                    arrayOfString.add(uri.toString())
                }
                // Remove [ and ] in the array
                val beforeArray = uriArray.toString().substring(1)
                val modifiedArray = beforeArray.substring(0, beforeArray.length - 1)
                transactionUri = modifiedArray
            }
        }

        addTransactionViewModel.removeFragment.observe(this){ remove ->
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
                transactionBudget, transactionCategory, transactionNote, transactionUri))
}