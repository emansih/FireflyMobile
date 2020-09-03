package xyz.hisname.fireflyiii.ui.tasker

import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.dinglisch.android.tasker.TaskerPlugin
import retrofit2.Retrofit
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.network.CustomCa

class GetTransactionRunner: TaskerPluginRunnerAction<GetTransactionInput, GetTransactionOutput>() {


    private lateinit var customCa: CustomCa
    private lateinit var sharedPref: SharedPreferences
    private lateinit var accountManager: AuthenticatorManager
    private val sslSocketFactory by lazy { customCa.getCustomSSL() }
    private val trustManager by lazy { customCa.getCustomTrust() }

    private fun genericService(): Retrofit? {
        val cert = AppPref(sharedPref).certValue
        return if (AppPref(sharedPref).isCustomCa) {
            FireflyClient.getClient(AppPref(sharedPref).baseUrl,
                    accountManager.accessToken, cert, trustManager, sslSocketFactory)
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
                     "transactionBudget", "transactionCategory"))
    }

    override fun run(context: Context, input: TaskerInput<GetTransactionInput>): TaskerPluginResult<GetTransactionOutput> {
        replaceVariable(input)
        accountManager = AuthenticatorManager(AccountManager.get(context))
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        customCa = CustomCa(("file://" + context.filesDir.path + "/user_custom.pem").toUri().toFile())
        val transactionType = input.regular.transactionType ?: ""
        val transactionDescription = input.regular.transactionDescription ?: ""
        val transactionAmount = input.regular.transactionAmount ?: ""
        val transactionTime = input.regular.transactionTime
        val transactionDate = input.regular.transactionDate ?: ""
        val transactionPiggyBank = input.regular.transactionPiggyBank
        val transactionSourceAccount = input.regular.transactionSourceAccount
        val transactionDestinationAccount = input.regular.transactionDestinationAccount
        val transactionCurrency = input.regular.transactionCurrency ?: ""
        val transactionTags = input.regular.transactionTags
        val transactionBudget = input.regular.transactionBudget
        val transactionCategory = input.regular.transactionCategory
        val dateTime = if (transactionTime == null) {
            transactionDate
        } else {
            DateTimeUtil.mergeDateTimeToIso8601(transactionDate, transactionTime)
        }
        var taskerResult: TaskerPluginResult<GetTransactionOutput>
        runBlocking(Dispatchers.IO){
            taskerResult = addTransaction(context, transactionType, transactionDescription, dateTime,
                    transactionPiggyBank, transactionAmount, transactionSourceAccount,
                    transactionDestinationAccount, transactionCurrency, transactionCategory,
                    transactionTags, transactionBudget) as TaskerPluginResult<GetTransactionOutput>
        }
        return taskerResult
    }

    private suspend fun addTransaction(context: Context, type: String, description: String,
                                       date: String, piggyBankName: String?, amount: String,
                                       sourceName: String?, destinationName: String?, currencyName: String,
                                       category: String?, tags: String?, budgetName: String?): TaskerPluginResult<Unit>{
        try {
            val response = genericService()?.create(TransactionService::class.java)?.suspendAddTransaction(convertString(type),
                    description, date, piggyBankName, amount.replace(',', '.'),
                    sourceName, destinationName, currencyName, category, tags, budgetName)
            val responseBody = response?.body()
            val errorBody = response?.errorBody()
            var errorBodyMessage = ""
            if (errorBody != null) {
                errorBodyMessage = String(errorBody?.bytes())
            }
            if (response?.isSuccessful == true && responseBody != null) {
                responseBody.data.transactionAttributes?.transactions?.forEach { transaction ->
                    val transactionDb = AppDatabase.getInstance(context).transactionDataDao()
                    transactionDb.insert(transaction)
                    transactionDb.insert(TransactionIndex(response.body()?.data?.transactionId,
                            transaction.transaction_journal_id))
                }
                return TaskerPluginResultSucess(GetTransactionOutput(response.body().toString()), null) as TaskerPluginResult<Unit>
            } else {
                return TaskerPluginResultError(0, errorBodyMessage)
            }
        } catch (exception: Exception){
            return TaskerPluginResultError(exception)
        }
    }

    private fun convertString(type: String) = type.substring(0, 1).toLowerCase() + type.substring(1).toLowerCase()
}