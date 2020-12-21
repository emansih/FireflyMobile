package xyz.hisname.fireflyiii.ui.tasker

import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import kotlinx.coroutines.runBlocking
import net.dinglisch.android.tasker.TaskerPlugin
import retrofit2.Retrofit
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.dao.CurrencyDataDao
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.attachment.AttachableType
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.network.CustomCa
import xyz.hisname.fireflyiii.workers.AttachmentWorker
import java.io.File

class GetTransactionRunner: TaskerPluginRunnerAction<GetTransactionInput, GetTransactionOutput>() {


    private lateinit var customCa: CustomCa
    private lateinit var sharedPref: SharedPreferences
    private lateinit var accountManager: AuthenticatorManager
    private lateinit var transactionDatabase: TransactionDataDao
    private lateinit var currencyDatabase: CurrencyDataDao

    private fun genericService(): Retrofit {
        val cert = AppPref(sharedPref).certValue
        return if (AppPref(sharedPref).isCustomCa) {
            FireflyClient.getClient(AppPref(sharedPref).baseUrl,
                    accountManager.accessToken, cert, customCa.getCustomTrust(), customCa.getCustomSSL())
        } else {
            FireflyClient.getClient(AppPref(sharedPref).baseUrl,
                    accountManager.accessToken, cert, null, null)
        }

    }

    override val notificationProperties get() = NotificationProperties()

    private fun replaceVariable(input: TaskerInput<GetTransactionInput>){
        TaskerPlugin.Setting.setVariableReplaceKeys(input.dynamic.bundle, arrayOf(
                     "transactionDescription", "transactionAmount", "transactionDate", "transactionTime",
                     "transactionPiggyBank", "transactionSourceAccount",
                     "transactionDestinationAccount", "transactionCurrency", "transactionTags",
                     "transactionBudget", "transactionCategory", "transactionNote", "transactionUri"))
    }

    override fun run(context: Context, input: TaskerInput<GetTransactionInput>): TaskerPluginResult<GetTransactionOutput> {
        replaceVariable(input)
        transactionDatabase = AppDatabase.getInstance(context).transactionDataDao()
        currencyDatabase = AppDatabase.getInstance(context).currencyDataDao()
        accountManager = AuthenticatorManager(AccountManager.get(context))
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        customCa = CustomCa(File(context.filesDir.path + "/user_custom.pem"))
        val transactionType = input.regular.transactionType ?: ""
        val transactionDescription = input.regular.transactionDescription ?: ""
        val transactionAmount = input.regular.transactionAmount ?: ""
        val transactionTime = input.regular.transactionTime
        val transactionDate = input.regular.transactionDate ?: ""
        val transactionPiggyBank = input.regular.transactionPiggyBank
        val transactionSourceAccount = input.regular.transactionSourceAccount ?: ""
        val transactionDestinationAccount = input.regular.transactionDestinationAccount ?: ""
        val transactionCurrency = input.regular.transactionCurrency ?: ""
        val transactionTags = input.regular.transactionTags
        val transactionBudget = input.regular.transactionBudget
        val transactionCategory = input.regular.transactionCategory
        val transactionNotes = input.regular.transactionNote
        val fileUri = input.regular.transactionUri
        val uriArray = arrayListOf<Uri>()
        fileUri.forEach { uri ->
            uriArray.add(uri.toUri())
        }
        var taskerResult: TaskerPluginResult<GetTransactionOutput>
        runBlocking {
            taskerResult = addTransaction(transactionType, transactionDescription, transactionDate, transactionTime,
                    transactionPiggyBank, transactionAmount, transactionSourceAccount,
                    transactionDestinationAccount, transactionCurrency, transactionCategory,
                    transactionTags, transactionBudget, transactionNotes, uriArray, context) as TaskerPluginResult<GetTransactionOutput>
        }
        return taskerResult
    }

    private suspend fun addTransaction(type: String, description: String,
                                       date: String, time: String?, piggyBankName: String?, amount: String,
                                       sourceName: String?, destinationName: String?, currencyName: String,
                                       category: String?, tags: String?, budgetName: String?,
                                       notes: String?, fileUri: List<Uri>, context: Context): TaskerPluginResult<Unit>{

        val transactionRepository = TransactionRepository(transactionDatabase, genericService().create(TransactionService::class.java))
        val addTransaction = transactionRepository.addTransaction(type,description, date, time,
                piggyBankName, amount, sourceName, destinationName, currencyName, category, tags, budgetName, notes)
        return when {
            addTransaction.response != null -> {
                if(fileUri.isNotEmpty()){
                    addTransaction.response.data.transactionAttributes.transactions.forEach { transactions ->
                        AttachmentWorker.initWorker(fileUri, transactions.transaction_journal_id,
                                context, AttachableType.TRANSACTION)
                    }
                }
                TaskerPluginResultSucess(GetTransactionOutput(addTransaction.response.toString())) as TaskerPluginResult<Unit>
            }
            addTransaction.errorMessage != null -> {
                TaskerPluginResultError(0, addTransaction.errorMessage)
            }
            addTransaction.error != null -> {
                TaskerPluginResultError(addTransaction.error)
            }
            else -> {
                TaskerPluginResultError(Exception("Failed to add $description"))
            }
        }
    }

}