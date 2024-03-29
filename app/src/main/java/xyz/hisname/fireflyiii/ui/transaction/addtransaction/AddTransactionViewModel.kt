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

package xyz.hisname.fireflyiii.ui.transaction.addtransaction

import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.lifecycle.*
import androidx.work.WorkInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.dao.TmpDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.*
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.repository.attachment.AttachableType
import xyz.hisname.fireflyiii.repository.attachment.AttachmentRepository
import xyz.hisname.fireflyiii.repository.bills.BillRepository
import xyz.hisname.fireflyiii.repository.budget.BudgetRepository
import xyz.hisname.fireflyiii.repository.category.CategoryRepository
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.repository.piggybank.PiggyRepository
import xyz.hisname.fireflyiii.repository.tags.TagsRepository
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.workers.AttachmentWorker
import xyz.hisname.fireflyiii.workers.transaction.TransactionWorker
import java.io.File
import java.net.UnknownHostException

class AddTransactionViewModel(application: Application): BaseViewModel(application) {


    private val temporaryDb = TmpDatabase.getInstance(application, getUniqueHash())
    private val transactionService = genericService().create(TransactionService::class.java)

    private val transactionRepository = TransactionRepository(
            AppDatabase.getInstance(application, getUniqueHash()).transactionDataDao(),
            transactionService
    )

    private val temporaryTransactionRepository = TransactionRepository(
            temporaryDb.transactionDataDao(), transactionService
    )

    private val currencyRepository = CurrencyRepository(
            AppDatabase.getInstance(application, getUniqueHash()).currencyDataDao(),
            genericService().create(CurrencyService::class.java)
    )
    private val accountRepository = AccountRepository(
            AppDatabase.getInstance(application, getUniqueHash()).accountDataDao(),
            genericService().create(AccountsService::class.java)
    )

    private val attachmentDao = AppDatabase.getInstance(getApplication(), getUniqueHash()).attachmentDataDao()
    private val attachmentService = genericService().create(AttachmentService::class.java)
    private val attachmentRepository = AttachmentRepository(attachmentDao, attachmentService)


    // We do lazy init here because a user might not type anything inside category edit text
    private val categoryRepository by lazy {
        CategoryRepository(AppDatabase.getInstance(application, getUniqueHash()).categoryDataDao(),
            genericService().create(CategoryService::class.java), null,
            genericService().create(SearchService::class.java))
    }
    private val piggyRepository by lazy {
        PiggyRepository(AppDatabase.getInstance(application, getUniqueHash()).piggyDataDao(),
                genericService().create(PiggybankService::class.java))
    }

    private val billRepository by lazy {
        BillRepository(AppDatabase.getInstance(application, getUniqueHash()).billDataDao(),
            genericService().create(BillsService::class.java))
    }

    private val budgetService by lazy { genericService().create(BudgetService::class.java) }
    private val spentDao by lazy { AppDatabase.getInstance(application, getUniqueHash()).spentDataDao() }
    private val budgetLimitDao by lazy { AppDatabase.getInstance(application, getUniqueHash()).budgetLimitDao() }
    private val budgetDao by lazy { AppDatabase.getInstance(application, getUniqueHash()).budgetDataDao() }
    private val budgetListDao by lazy { AppDatabase.getInstance(application, getUniqueHash()).budgetListDataDao() }
    private val budgetRepository by lazy {
        BudgetRepository(budgetDao, budgetListDao, spentDao, budgetLimitDao, budgetService)
    }

    private val tagsRepository by lazy {
        TagsRepository(
            AppDatabase.getInstance(application, getUniqueHash()).tagsDataDao(),
            genericService().create(TagsService::class.java),
            genericService().create(SearchService::class.java))
    }

    var currency = ""

    val transactionAmount = MutableLiveData<String>()
    val transactionType = MutableLiveData<String>()
    val transactionDescription = MutableLiveData<String>()
    val transactionDate = MutableLiveData<String>()
    val transactionTime = MutableLiveData<String>()
    val transactionPiggyBank = MutableLiveData<String?>()
    val transactionSourceAccount = MutableLiveData<String>()
    val transactionDestinationAccount = MutableLiveData<String>()
    val transactionCurrency = MutableLiveData<String>()
    val transactionCategory = MutableLiveData<String>()
    val transactionTags = MutableLiveData<String?>()
    val transactionBudget = MutableLiveData<String>()
    val transactionBill = MutableLiveData<String>()
    val transactionNote = MutableLiveData<String>()
    val fileUri = MutableLiveData<List<Uri>>()
    val removeFragment = MutableLiveData<Boolean>()
    val transactionBundle = MutableLiveData<Bundle>()
    val isFromTasker = MutableLiveData<Boolean>()
    val saveData = MutableLiveData<Boolean>()
    val increaseTab = MutableLiveData<Boolean>()
    val decreaseTab = MutableLiveData<Boolean>()
    var numTabs = 0
    val transactionAttachment = MutableLiveData<List<AttachmentData>>()

    private var transactionMasterId = 0L

    fun saveData(masterId: Long){
        transactionMasterId = masterId
        isLoading.postValue(true)
        saveData.postValue(true)
    }

    fun parseBundle(bundle: Bundle?){
        if(bundle != null){
            val currencyBundle = bundle.getString("transactionCurrency")
            if(currencyBundle?.startsWith("%") == false){
                viewModelScope.launch(Dispatchers.IO){
                    val currencyAttributes = currencyRepository.getCurrencyByCode(currencyBundle)[0].currencyAttributes
                    transactionCurrency.postValue(currencyAttributes.name + " (" + currencyAttributes.code + ")")
                }
            } else {
                // Is tasker variable
                transactionCurrency.postValue(currencyBundle ?: "")
            }
            transactionDescription.postValue(bundle.getString("transactionDescription"))
            transactionAmount.postValue(bundle.getString("transactionAmount"))
            if(bundle.getString("transactionDate") == null){
                transactionDate.postValue(DateTimeUtil.getTodayDate())
            } else {
                transactionDate.postValue(bundle.getString("transactionDate"))
            }
            transactionTime.postValue(bundle.getString("transactionTime"))
            transactionPiggyBank.postValue(bundle.getString("transactionPiggyBank"))
            transactionTags.postValue(bundle.getString("transactionTags"))
            transactionBudget.postValue(bundle.getString("transactionBudget"))
            transactionCategory.postValue(bundle.getString("transactionCategory"))
            transactionBill.postValue(bundle.getString("transactionBill"))
            transactionNote.postValue(bundle.getString("transactionNote"))
            transactionSourceAccount.postValue(bundle.getString("transactionSourceAccount"))
            transactionDestinationAccount.postValue(bundle.getString("transactionDestinationAccount"))
            val transactionUriBundle = bundle.getString("transactionUri")
            if(!transactionUriBundle.isNullOrEmpty()){
                val uriArray = arrayListOf<Uri>()
                val arrayOfString = transactionUriBundle.split(",")
                arrayOfString.forEach { uri ->
                    uriArray.add(uri.trim().toUri())
                }
                fileUri.postValue(uriArray)
            }
        }
    }

    fun getTransactionFromJournalId(transactionJournalId: Long){
        viewModelScope.launch(Dispatchers.IO){
            val transactionList = transactionRepository.getTransactionByJournalId(transactionJournalId)
            transactionDescription.postValue(transactionList.description)
            transactionBudget.postValue(transactionList.budget_name ?: "")
            transactionAmount.postValue(transactionList.amount.toString())
            transactionCurrency.postValue(transactionList.currency_name +
                    " (" + transactionList.currency_code + " )")
            transactionDate.postValue(DateTimeUtil.convertIso8601ToHumanDate(transactionList.date))
            transactionCategory.postValue(transactionList.category_name ?: "")
            transactionTime.postValue(DateTimeUtil.convertIso8601ToHumanTime(transactionList.date))
            transactionNote.postValue(transactionList.notes ?: "")
            if(transactionList.tags.isNotEmpty()){
                transactionTags.postValue(transactionList.tags.toString())
            }
            transactionBill.postValue(transactionList.bill_name ?: "")
            transactionSourceAccount.postValue(transactionList.source_name ?: "")
            transactionDestinationAccount.postValue(transactionList.destination_name)
            transactionAttachment.postValue(transactionRepository.getAttachment(transactionJournalId, attachmentDao))
        }
    }

    fun uploadFile(transactionJournalId: Long, fileToUpload: ArrayList<Uri>): LiveData<List<WorkInfo>>{
        return AttachmentWorker.initWorker(fileToUpload, transactionJournalId,
                getApplication<Application>(), AttachableType.TRANSACTION, getUniqueHash())
    }

    fun deleteAttachment(data: AttachmentData): LiveData<Boolean>{
        val isSuccessful = MutableLiveData<Boolean>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            isLoading.postValue(false)
            val fileName = getApplication<Application>().getExternalFilesDir(null).toString() +
                    File.separator + data.attachmentAttributes.filename
            attachmentRepository.deleteAttachment(data, fileName)
        }
        return isSuccessful
    }

    fun memoryCount() = temporaryTransactionRepository.getPendingTransactionFromId(transactionMasterId)

    fun uploadTransaction(groupTitle: String): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        viewModelScope.launch{
            val addTransaction = temporaryTransactionRepository.addSplitTransaction(groupTitle, transactionMasterId)
            isLoading.postValue(false)
            when {
                addTransaction.response != null -> {
                    apiResponse.postValue(Pair(true,
                            getApplication<Application>().resources.getString(R.string.transaction_added)))
                    addTransaction.response.data.transactionAttributes.transactions.forEach { transaction ->
                        if(!transaction.internal_reference.isNullOrEmpty()){
                            val uriArray = temporaryTransactionRepository.getTemporaryAttachment(transaction.internal_reference.toLong())
                            if(!uriArray.isNullOrEmpty()){
                                // Remove [[" and "]] in the string
                                val beforeArray = uriArray.toString().substring(3)
                                val modifiedArray = beforeArray.substring(0, beforeArray.length - 3)
                                val arrayOfString = modifiedArray.split(",")
                                val arrayOfUri = arrayListOf<Uri>()
                                arrayOfString.forEach { array ->
                                    arrayOfUri.add(array.toUri())
                                }
                                uploadFile(transaction.transaction_journal_id, arrayOfUri)
                            }
                            temporaryTransactionRepository.removeInternalReference(addTransaction.response)
                        }
                        transactionRepository.insertTransaction(transaction)
                        transactionRepository.insertTransaction(TransactionIndex(
                                0,
                                addTransaction.response.data.transactionId,
                                transaction.transaction_journal_id,
                                addTransaction.response.data.transactionAttributes.group_title))
                    }
                    temporaryTransactionRepository.deletePendingTransactionFromId(transactionMasterId)
                }
                addTransaction.errorMessage != null -> {
                    apiResponse.postValue(Pair(false, addTransaction.errorMessage))
                    temporaryTransactionRepository.deletePendingTransactionFromId(transactionMasterId)
                }
                addTransaction.error != null -> {
                    if(addTransaction.error is UnknownHostException){
                        TransactionWorker.initWorker(getApplication(), groupTitle,
                                transactionMasterId, getUniqueHash())
                        apiResponse.postValue(Pair(true,
                                getApplication<Application>().getString(R.string.data_added_when_user_online,
                                        groupTitle)))
                    } else {
                        apiResponse.postValue(Pair(false, addTransaction.error.localizedMessage))
                        temporaryTransactionRepository.deletePendingTransactionFromId(transactionMasterId)
                    }
                }
                else -> {
                    apiResponse.postValue(Pair(false, "Error adding transaction"))
                    temporaryTransactionRepository.deletePendingTransactionFromId(transactionMasterId)
                }
            }
        }
        return apiResponse
    }

    fun addTransaction(type: String, description: String,
                       date: String, time: String, piggyBankName: String?, amount: String,
                       sourceName: String?, destinationName: String?,
                       category: String?, tags: String?, budgetName: String?, billName: String?,
                       fileUri: ArrayList<Uri>, notes: String){
        viewModelScope.launch(Dispatchers.IO){
            temporaryTransactionRepository.storeSplitTransaction(type,description, date, time, piggyBankName,
                    amount.replace(',', '.'), sourceName, destinationName, currency,
                    category, tags, budgetName, billName, notes, fileUri, transactionMasterId)
        }
    }

    fun updateTransaction(transactionId: Long, type: String, description: String,
                          date: String, time: String, piggyBankName: String?, amount: String,
                          sourceName: String?, destinationName: String?,
                          category: String?, tags: String?, budgetName: String?, billName: String?,
                          notes: String): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            val addTransaction = transactionRepository.updateTransaction(transactionId, type,description, date, time, piggyBankName,
                    amount.replace(',', '.'), sourceName, destinationName, currency,
                    category, tags, budgetName, billName, notes)
            when {
                addTransaction.response != null -> {
                    apiResponse.postValue(Pair(true,
                            getApplication<Application>().resources.getString(R.string.transaction_updated)))
                    // TODO: Check if there is any changes to attachment
                }
                addTransaction.errorMessage != null -> {
                    apiResponse.postValue(Pair(false, addTransaction.errorMessage))
                }
                addTransaction.error != null -> {
                    apiResponse.postValue(Pair(false, addTransaction.error.localizedMessage))
                }
                else -> {
                    apiResponse.postValue(Pair(false, "Error updating transaction"))
                }
            }
            isLoading.postValue(false)
        }
        return apiResponse
    }

    fun getAccounts(): LiveData<List<String>>{
        val accountData: MutableLiveData<List<String>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            isLoading.postValue(true)
            val accountList = arrayListOf<String>()
            accountRepository.getAccountByType("asset").forEach {  data ->
                data.accountAttributes.name.let { accountList.add(it) }
            }
            accountData.postValue(accountList)
            isLoading.postValue(false)
        }
        return accountData
    }

    fun getCategory(categoryName: String): LiveData<List<String>>{
        val categoryLiveData: MutableLiveData<List<String>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            val categoryList = arrayListOf<String>()
            categoryRepository.searchCategoryByName(categoryName).forEach { categoryData ->
                categoryList.add(categoryData.categoryAttributes.name)
            }
            categoryLiveData.postValue(categoryList)

        }
        return categoryLiveData
    }

    fun getTransactionByDescription(query: String) : LiveData<List<String>>{
        val transactionData: MutableLiveData<List<String>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            transactionData.postValue(transactionRepository.getTransactionByDescription(query))
        }
        return transactionData
    }

    fun getAccountByNameAndType(accountType: String, accountName: String): LiveData<List<String>>{
        val accountData: MutableLiveData<List<String>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            accountData.postValue(accountRepository.getAccountByNameAndType(accountType, accountName))
        }
        return accountData
    }

    fun getPiggyBank(): LiveData<List<String>>{
        val piggyLiveData: MutableLiveData<List<String>> = MutableLiveData()
        val mutatedPiggyListList = arrayListOf<String>()
        viewModelScope.launch(Dispatchers.IO){
            piggyRepository.getPiggyNames().map { piggyList ->
                // Add a blank entry so that user can "unselect" piggy bank
                mutatedPiggyListList.add("")
                mutatedPiggyListList.addAll(piggyList)
            }.collectLatest {
                // We need `collectLatest` otherwise data won't be posted. Don't put postValue()
                // inside map
                piggyLiveData.postValue(mutatedPiggyListList)
            }
        }
        return piggyLiveData
    }

    fun getBudget(): LiveData<List<String>>{
        val data: MutableLiveData<List<String>> = MutableLiveData()
        val mutatedBudgetList = arrayListOf<String>()
        viewModelScope.launch(Dispatchers.IO) {
            // Add a blank entry so that user can "unselect" budget
            mutatedBudgetList.add("")
            budgetRepository.getAllBudgetFlow("", "").map { budgetList ->
                budgetList.forEach { data ->
                    mutatedBudgetList.add(data.budgetListAttributes.name)
                }
            }.collectLatest {
                data.postValue(mutatedBudgetList)
            }
        }
        return data
    }

    fun getDefaultCurrency(): LiveData<String>{
        val currencyLiveData = MutableLiveData<String>()
        viewModelScope.launch(Dispatchers.IO){
            val currencyList = currencyRepository.defaultCurrency().currencyAttributes
            currency = currencyList.code
            currencyLiveData.postValue(currencyList.name + " (" + currencyList.code + ")")
        }
        return currencyLiveData
    }

    fun getTags(tagName: String): LiveData<List<String>> {
        val tagsLiveData = MutableLiveData<List<String>>()
        viewModelScope.launch(Dispatchers.IO){
            tagsLiveData.postValue(tagsRepository.searchTag(tagName))
        }
        return tagsLiveData
    }

    fun getAllBills(): LiveData<List<String>>{
        val billsLiveData = MutableLiveData<List<String>>()
        val mutatedBillList = arrayListOf<String>()
        viewModelScope.launch(Dispatchers.IO) {
            billRepository.getAllBills().map {  billList ->
                // Add a blank entry so that user can "unselect" bill
                mutatedBillList.add("")
                mutatedBillList.addAll(billList)
            }.collectLatest {
                // We need `collectLatest` otherwise data won't be posted. Don't put postValue()
                // inside map
                billsLiveData.postValue(mutatedBillList)
            }
        }
        return billsLiveData
    }
}