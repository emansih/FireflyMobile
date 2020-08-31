package xyz.hisname.fireflyiii.receiver

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.TaskerPlugin
import xyz.hisname.fireflyiii.util.network.CustomCa
import xyz.hisname.fireflyiii.workers.transaction.AttachmentWorker

class PluginReceiver: AbstractPluginSettingReceiver(){

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

    override fun isBundleValid(bundle: Bundle): Boolean {
        bundle.getString("transactionDescription") ?: return false
        val transactionType = bundle.getString("transactionType") ?: return false
        bundle.getString("transactionAmount") ?: return false
        bundle.getString("transactionDate") ?: return false
        val sourceAccount = bundle.getString("transactionSourceAccount")
        val destinationAccount = bundle.getString("transactionDestinationAccount")
        if(transactionType.contentEquals("Deposit") && transactionType.contentEquals("Transfer")){
            return destinationAccount != null
        }
        // Deposit does not needs source account
        if(transactionType.contentEquals("Withdrawal") && transactionType.contentEquals("Transfer")){
            return sourceAccount != null
        }
        bundle.getString("transactionCurrency") ?: return false
        return true
    }

    override val isAsync: Boolean
        get() = false

    override fun firePluginSetting(context: Context, intent: Intent, bundle: Bundle) {
        accountManager = AuthenticatorManager(AccountManager.get(context))
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        customCa = CustomCa(("file://" + context.filesDir.path + "/user_custom.pem").toUri().toFile())
        val transactionTypeBundle = bundle.getString("transactionType") ?: ""
        val transactionDescription = bundle.getString("transactionDescription") ?: ""
        val transactionAmount = bundle.getString("transactionAmount") ?: ""
        val transactionTime = bundle.getString("transactionTime")
        val transactionDate = bundle.getString("transactionDate") ?: ""
        val transactionPiggyBank = bundle.getString("transactionPiggyBank")
        val transactionSourceAccount = bundle.getString("transactionSourceAccount")
        val transactionDestinationAccount = bundle.getString("transactionDestinationAccount") ?: ""
        val transactionCurrency = bundle.getString("transactionCurrency") ?: ""
        val transactionTags = bundle.getString("transactionTags")
        val transactionBudget = bundle.getString("transactionBudget")
        val transactionCategory = bundle.getString("transactionCategory")
        val fileUri = bundle.getString("fileUri")
        val dateTime = if (transactionTime == null) {
            transactionDate
        } else {
            DateTimeUtil.mergeDateTimeToIso8601(transactionDate, transactionTime)
        }
        runBlocking(Dispatchers.IO) {
            addTransaction(context, transactionTypeBundle, transactionDescription, dateTime,
                    transactionPiggyBank, transactionAmount, transactionSourceAccount,
                    transactionDestinationAccount, transactionCurrency, transactionCategory,
                    transactionTags, transactionBudget, fileUri?.toUri(), intent)
        }
    }


    private suspend fun addTransaction(context: Context, type: String, description: String,
                                       date: String, piggyBankName: String?, amount: String,
                                       sourceName: String?, destinationName: String?, currencyName: String,
                                       category: String?, tags: String?, budgetName: String?, fileUri: Uri?, intent: Intent){
        try {
            val response = genericService()?.create(TransactionService::class.java)?.suspendAddTransaction(convertString(type),
                    description, date, piggyBankName, amount.replace(',', '.'),
                    sourceName, destinationName, currencyName, category, tags, budgetName)
            val responseBody = response?.body()
            val errorBody = response?.errorBody()
            var errorBodyMessage = ""
            if (errorBody != null) {
                errorBodyMessage = String(errorBody.bytes())
                val gson = Gson().fromJson(errorBodyMessage, ErrorModel::class.java)
                errorBodyMessage = when {
                    gson.errors.transactions_currency != null -> "Currency Code Required"
                    gson.errors.piggy_bank_name != null -> "Invalid Piggy Bank Name"
                    gson.errors.transactions_destination_name != null -> "Invalid Destination Account"
                    gson.errors.transactions_source_name != null -> "Invalid Source Account"
                    gson.errors.transaction_destination_id != null -> gson.errors.transaction_destination_id[0]
                    gson.errors.transaction_amount != null -> "Amount field is required"
                    gson.errors.description != null -> "Description is required"
                    else -> "Error occurred while saving transaction"
                }
            }
            if (response?.isSuccessful == true && responseBody != null) {
                var journalId: Long = 0
                responseBody.data.transactionAttributes?.transactions?.forEach { transaction ->
                    val transactionDb = AppDatabase.getInstance(context).transactionDataDao()
                    transactionDb.insert(transaction)
                    transactionDb.insert(TransactionIndex(response.body()?.data?.transactionId,
                            transaction.transaction_journal_id))
                    journalId = transaction.transaction_journal_id
                }
                if(fileUri != null){
                    AttachmentWorker.initWorker(fileUri, journalId, context)
                }
                val bundle = bundleOf("%response" to responseBody.toString())
                TaskerPlugin.Setting.signalFinish(context, intent, TaskerPlugin.Setting.RESULT_CODE_OK, bundle)
            } else {
                val bundle = bundleOf(TaskerPlugin.Setting.VARNAME_ERROR_MESSAGE to errorBodyMessage)
                TaskerPlugin.Setting.signalFinish(
                        context,
                        intent,
                        TaskerPlugin.Setting.RESULT_CODE_FAILED,
                        bundle
                )

            }
        } catch (exception: Exception){
            val bundle = bundleOf(TaskerPlugin.Setting.VARNAME_ERROR_MESSAGE to exception.localizedMessage)
            TaskerPlugin.Setting.signalFinish(
                    context,
                    intent,
                    TaskerPlugin.Setting.RESULT_CODE_FAILED,
                    bundle
            )
        }
    }

    private fun convertString(type: String) = type.substring(0, 1).toLowerCase() + type.substring(1).toLowerCase()
}