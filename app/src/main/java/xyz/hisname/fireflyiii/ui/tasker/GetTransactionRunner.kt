/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.ui.tasker

import android.accounts.AccountManager
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.net.toUri
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import kotlinx.coroutines.runBlocking
import net.dinglisch.android.tasker.TaskerPlugin
import retrofit2.Retrofit
import xyz.hisname.fireflyiii.data.local.account.OldAuthenticatorManager
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.dao.CurrencyDataDao
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.attachment.AttachableType
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.getUniqueHash
import xyz.hisname.fireflyiii.util.network.CustomCa
import xyz.hisname.fireflyiii.workers.AttachmentWorker
import java.io.File

class GetTransactionRunner: TaskerPluginRunnerAction<GetTransactionInput, GetTransactionOutput>() {


    private lateinit var customCa: CustomCa
    private lateinit var sharedPref: SharedPreferences
    private lateinit var accountManager: OldAuthenticatorManager
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
                     "transactionBudget", "transactionCategory", "transactionBill", "transactionNote", "transactionUri"))
    }

    override fun run(context: Context, input: TaskerInput<GetTransactionInput>): TaskerPluginResult<GetTransactionOutput> {
        replaceVariable(input)
        transactionDatabase = AppDatabase.getInstance(context, context.getUniqueHash()).transactionDataDao()
        currencyDatabase = AppDatabase.getInstance(context, context.getUniqueHash()).currencyDataDao()
        accountManager = OldAuthenticatorManager(AccountManager.get(context))
        sharedPref = context.getSharedPreferences(
            context.getUniqueHash().toString() + "-user-preferences", Context.MODE_PRIVATE)
        customCa = CustomCa(File(context.filesDir.path + "/" + context.getUniqueHash() + ".pem"))
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
        val transactionBill = input.regular.transactionBill
        val transactionNotes = input.regular.transactionNote
        val fileUri = input.regular.transactionUri
        val uriArray = arrayListOf<Uri>()
        val arrayOfString = fileUri?.split(",")
        arrayOfString?.forEach { uri ->
            uriArray.add(uri.trim().toUri())
        }
        var taskerResult: TaskerPluginResult<GetTransactionOutput>
        runBlocking {
            taskerResult = addTransaction(transactionType, transactionDescription, transactionDate, transactionTime,
                    transactionPiggyBank, transactionAmount, transactionSourceAccount,
                    transactionDestinationAccount, transactionCurrency, transactionCategory,
                    transactionTags, transactionBudget, transactionBill,
                    transactionNotes, uriArray, context) as TaskerPluginResult<GetTransactionOutput>
        }
        return taskerResult
    }

    private suspend fun addTransaction(type: String, description: String,
                                       date: String, time: String?, piggyBankName: String?, amount: String,
                                       sourceName: String?, destinationName: String?, currencyName: String,
                                       category: String?, tags: String?, budgetName: String?, billName: String?,
                                       notes: String?, fileUri: List<Uri>, context: Context): TaskerPluginResult<Unit>{

        val transactionRepository = TransactionRepository(transactionDatabase, genericService().create(TransactionService::class.java))
        val addTransaction = transactionRepository.addTransaction(type,description, date, time,
                piggyBankName, amount, sourceName, destinationName, currencyName, category, tags, budgetName, billName, notes)
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