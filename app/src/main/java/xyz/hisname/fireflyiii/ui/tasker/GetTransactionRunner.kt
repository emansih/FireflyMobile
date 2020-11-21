package xyz.hisname.fireflyiii.ui.tasker

import android.accounts.AccountManager
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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
import xyz.hisname.fireflyiii.data.local.dao.CurrencyDataDao
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.network.CustomCa
import java.io.File
import java.time.OffsetDateTime
import java.util.concurrent.ThreadLocalRandom

class GetTransactionRunner: TaskerPluginRunnerAction<GetTransactionInput, GetTransactionOutput>() {


    private lateinit var customCa: CustomCa
    private lateinit var sharedPref: SharedPreferences
    private lateinit var accountManager: AuthenticatorManager
    private lateinit var transactionDatabase: TransactionDataDao
    private lateinit var currencyDatabase: CurrencyDataDao

    private fun genericService(): Retrofit? {
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
                     "transactionBudget", "transactionCategory", "transactionNotes"))
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
        val dateTime = if (transactionTime == null) {
            transactionDate
        } else {
            DateTimeUtil.mergeDateTimeToIso8601(transactionDate, transactionTime)
        }
        val tagsList = arrayListOf<String>()
        if(transactionTags != null){
            tagsList.addAll(transactionTags.split(",").map { it.trim() })
        }
        var taskerResult: TaskerPluginResult<GetTransactionOutput>
        runBlocking(Dispatchers.IO){
            addTransactionToDb(transactionType, transactionDescription, transactionDate, transactionTime, transactionPiggyBank,
                    transactionAmount, transactionSourceAccount, transactionDestinationAccount, transactionCurrency,
            transactionCategory, tagsList, transactionBudget, transactionNotes)
            taskerResult = addTransaction(context, transactionType, transactionDescription, dateTime,
                    transactionPiggyBank, transactionAmount, transactionSourceAccount,
                    transactionDestinationAccount, transactionCurrency, transactionCategory,
                    transactionTags, transactionBudget, transactionNotes) as TaskerPluginResult<GetTransactionOutput>
        }
        return taskerResult
    }

    private fun addTransactionToDb(type: String, description: String,
                                   date: String, time: String?, piggyBankName: String?, amount: String,
                                   sourceName: String?, destinationName: String, currencyName: String,
                                   category: String?, tags: List<String>, budgetName: String?, notes: String?){
        val dateTime = if(time.isNullOrEmpty()){
            DateTimeUtil.offsetDateTimeWithoutTime(date)
        } else {
            DateTimeUtil.mergeDateTimeToIso8601(date, time)
        }
        var currency: CurrencyData
        runBlocking(Dispatchers.IO) {
            currency = currencyDatabase.getCurrencyByCode(currencyName)[0]
        }
        val transactionId = ThreadLocalRandom.current().nextLong()
        transactionDatabase.insert(
                Transactions(
                        transactionId, amount.toDouble(),  0,
                        budgetName, 0,  category, currency.currencyAttributes?.code ?: "",
                        currency.currencyAttributes?.decimal_places ?: 0, currency.currencyId ?: 0,
                        currency.currencyAttributes?.name ?: "", currency.currencyAttributes?.symbol ?: "",
                        OffsetDateTime.parse(dateTime), description, 0, destinationName,
                        "", "",  0.0, "","", 0,
                        "", notes, 0, "", 0,
                        sourceName, "", tags, type, 0, piggyBankName,true)
        )
    }

    private suspend fun addTransaction(context: Context, type: String, description: String,
                                       date: String, piggyBankName: String?, amount: String,
                                       sourceName: String?, destinationName: String?, currencyName: String,
                                       category: String?, tags: String?, budgetName: String?, notes: String?): TaskerPluginResult<Unit>{
        try {
            val response = genericService()?.create(TransactionService::class.java)?.suspendAddTransaction(convertString(type),
                    description, date, piggyBankName, amount.replace(',', '.'),
                    sourceName, destinationName, currencyName, category, tags, budgetName, notes)
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