package xyz.hisname.fireflyiii.receiver

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.twofortyfouram.locale.sdk.client.receiver.AbstractPluginSettingReceiver
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.TaskerPlugin
import xyz.hisname.fireflyiii.util.extension.getString
import xyz.hisname.fireflyiii.util.network.CustomCa
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.workers.transaction.AttachmentWorker

class PluginReceiver: AbstractPluginSettingReceiver(){

    private lateinit var customCa: CustomCa
    private lateinit var sharedPref: SharedPreferences
    private lateinit var accountManager: AuthenticatorManager
    private val sslSocketFactory by lazy { customCa.getCustomSSL() }
    private val trustManager by lazy { customCa.getCustomTrust() }

    private fun genericService(): Retrofit? {
        var cert = AppPref(sharedPref).certValue
        return if (AppPref(sharedPref).isCustomCa) {
            FireflyClient.getClient(AppPref(sharedPref).baseUrl,
                    accountManager.accessToken, cert, trustManager, sslSocketFactory)
        } else {
            FireflyClient.getClient(AppPref(sharedPref).baseUrl,
                    accountManager.accessToken, cert, null, null)
        }

    }


    override fun isAsync() = true

    override fun isBundleValid(bundle: Bundle): Boolean {
        bundle.getString("transactionDescription") ?: return false
        bundle.getString("transactionType") ?: return false
        bundle.getString("transactionAmount") ?: return false
        bundle.getString("transactionDate") ?: return false
        bundle.getString("transactionSourceAccount") ?: return false
        bundle.getString("transactionDestinationAccount") ?: return false
        bundle.getString("transactionCurrency") ?: return false
        return true
    }

    override fun firePluginSetting(context: Context, bundle: Bundle) {
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
        val dateTime = if(transactionTime == null){
            transactionDate
        } else {
            DateTimeUtil.mergeDateTimeToIso8601(transactionDate, transactionTime)
        }
        addTransaction(context, transactionTypeBundle, transactionDescription, dateTime,
                transactionPiggyBank, transactionAmount, transactionSourceAccount,
                transactionDestinationAccount, transactionCurrency, transactionCategory,
                transactionTags, transactionBudget, fileUri?.toUri())
    }

    private fun addTransaction(context: Context, type: String, description: String,
                       date: String, piggyBankName: String?, amount: String,
                       sourceName: String?, destinationName: String?, currencyName: String,
                       category: String?, tags: String?, budgetName: String?, fileUri: Uri?){
        genericService()?.create(TransactionService::class.java)?.addTransaction(convertString(type),description, date ,piggyBankName,
                amount.replace(',', '.'),sourceName,destinationName,currencyName, category, tags, budgetName)?.enqueue(retrofitCallback({ response ->
            val responseBody = response.body()
            if (response.isSuccessful && responseBody != null) {
                var journalId: Long = 0
                runBlocking(Dispatchers.IO){
                    responseBody.data.transactionAttributes?.transactions?.forEach { transaction ->
                        val transactionDb = AppDatabase.getInstance(context).transactionDataDao()
                        transactionDb.insert(transaction)
                        transactionDb.insert(TransactionIndex(response.body()?.data?.transactionId,
                                transaction.transaction_journal_id))
                        journalId = transaction.transaction_journal_id
                    }
                }
                if(fileUri != null){
                    AttachmentWorker.initWorker(fileUri, journalId, context)
                }
                TaskerPlugin.Setting.signalFinish(context, Intent(), TaskerPlugin.Setting.RESULT_CODE_OK, null)
            } else {
                TaskerPlugin.Setting.signalFinish(context, Intent(), TaskerPlugin.Setting.RESULT_CODE_FAILED, null)
            }
        }))
    }

    private fun convertString(type: String) = type.substring(0,1).toLowerCase() + type.substring(1).toLowerCase()

}