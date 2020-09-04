package xyz.hisname.fireflyiii.repository.transaction

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.attachment.AttachmentRepository
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.transaction.*
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionSuccessModel
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.LocaleNumberParser
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.workers.transaction.AttachmentWorker
import xyz.hisname.fireflyiii.workers.transaction.DeleteTransactionWorker
import java.math.BigDecimal
import kotlin.math.absoluteValue

class TransactionsViewModel(application: Application): BaseViewModel(application) {

    val repository: TransactionRepository
    val transactionAmount: MutableLiveData<String> = MutableLiveData()
    private val transactionService by lazy { genericService()?.create(TransactionService::class.java) }

    init {
        val transactionDataDao = AppDatabase.getInstance(application).transactionDataDao()
        repository = TransactionRepository(transactionDataDao, transactionService)
    }

    fun getTransactionList(startDate: String?, endDate: String?, transactionType: String, pageNumber: Int): LiveData<MutableList<Transactions>> {
        isLoading.value = true
        val data: MutableLiveData<MutableList<Transactions>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            repository.transactionList(startDate, endDate, transactionType, pageNumber).collectLatest { transactions ->
                data.postValue(transactions)
            }
        }.invokeOnCompletion {
            isLoading.postValue(false)
        }
        return data
    }

    fun getRecentTransaction(limit: Int): LiveData<MutableList<Transactions>>{
        isLoading.value = true
        var recentData: MutableList<Transactions> = arrayListOf()
        val data: MutableLiveData<MutableList<Transactions>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            recentData = repository.recentTransactions(limit)
        }.invokeOnCompletion {
            data.postValue(recentData)
        }
        return data
    }

    fun getWithdrawalAmountWithCurrencyCode(startDate: String, endDate: String, currencyCode: String): LiveData<Double>{
        var withdrawData = 0.0
        val data: MutableLiveData<Double> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            withdrawData = repository.allWithdrawalWithCurrencyCode(startDate, endDate, currencyCode)
        }.invokeOnCompletion {
            data.postValue(LocaleNumberParser.parseDecimal(withdrawData, getApplication()).absoluteValue)
        }
        return data
    }

    fun getDepositAmountWithCurrencyCode(startDate: String, endDate: String, currencyCode: String): LiveData<Double>{
        var depositData = 0.0
        val data: MutableLiveData<Double> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            depositData = repository.allDepositWithCurrencyCode(startDate, endDate, currencyCode)
        }.invokeOnCompletion {
            data.postValue(LocaleNumberParser.parseDecimal(depositData, getApplication()).absoluteValue)
        }
        return data
    }

    // My god.... the name of this function is sooooooo looong...
    fun getTransactionsByAccountAndCurrencyCodeAndDate(startDate: String, endDate: String,
                                                               currencyCode: String,
                                                               accountName: String): LiveData<BigDecimal>{
        var transactionAmount: BigDecimal = 0.toBigDecimal()
        val data: MutableLiveData<BigDecimal> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionAmount = repository.getTransactionsByAccountAndCurrencyCodeAndDate(startDate, endDate, currencyCode, accountName)
        }.invokeOnCompletion {
            data.postValue(transactionAmount)
        }
        return data
    }

    fun getUniqueCategoryByDate(startDate: String, endDate: String, currencyCode: String,
                                sourceName: String, transactionType: String): MutableLiveData<MutableList<String>>{
        var transactionData: MutableList<String> = arrayListOf()
        val data: MutableLiveData<MutableList<String>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionData = repository.getUniqueCategoryByDate(startDate, endDate, currencyCode, sourceName, transactionType)
        }.invokeOnCompletion {
            data.postValue(transactionData)
        }
        return data
    }

    fun getUniqueBudgetByDate(startDate: String, endDate: String, currencyCode: String,
                              sourceName: String, transactionType: String): MutableLiveData<MutableList<String>>{
        var transactionData: MutableList<String> = arrayListOf()
        val data: MutableLiveData<MutableList<String>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionData = repository.getUniqueBudgetByDate(startDate, endDate, currencyCode, sourceName, transactionType)
        }.invokeOnCompletion {
            data.postValue(transactionData)
        }
        return data
    }

    fun getUniqueBudgetByDate(startDate: String, endDate: String, currencyCode: String,
                              transactionType: String): MutableLiveData<MutableList<String?>>{
        var transactionData: MutableList<String?> = arrayListOf()
        val data: MutableLiveData<MutableList<String?>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionData = repository.getUniqueBudgetByDate(startDate, endDate, currencyCode, transactionType)
        }.invokeOnCompletion {
            data.postValue(transactionData)
        }
        return data
    }

    fun getTotalTransactionAmountByDateAndCurrency(startDate: String, endDate: String,
                                             currencyCode: String, accountName: String,
                                                   transactionType: String): MutableLiveData<Double>{
        var transactionAmount = 0.0
        val data: MutableLiveData<Double> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionAmount = repository.getTotalTransactionType(startDate, endDate,
                    currencyCode, accountName, transactionType)
        }.invokeOnCompletion {
            data.postValue(transactionAmount)
        }
        return data
    }

    fun getTotalTransactionAmountByDateAndCurrency(startDate: String, endDate: String,
                                                   currencyCode: String,
                                                   transactionType: String): MutableLiveData<Double>{
        var transactionAmount = 0.0
        val data: MutableLiveData<Double> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionAmount = repository.getTotalTransactionType(startDate, endDate,
                    currencyCode, transactionType)
        }.invokeOnCompletion {
            data.postValue(transactionAmount)
        }
        return data
    }

    fun getTotalTransactionAmountAndFreqByDateAndCurrency(startDate: String, endDate: String,
                                                          currencyCode: String,
                                                          transactionType: String,
                                                          currencySymbol: String): MutableLiveData<TransactionAmountMonth>{
        var transactionAmount = 0.0
        var transactionFreq = 0
        val transactionData: MutableLiveData<TransactionAmountMonth> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionAmount = repository.getTotalTransactionType(startDate, endDate,
                    currencyCode, transactionType)
            transactionFreq = repository.transactionList(startDate, endDate, transactionType).size
        }.invokeOnCompletion {
            transactionData.postValue(TransactionAmountMonth(DateTimeUtil.getMonthAndYear(startDate),
                    currencySymbol + LocaleNumberParser.parseDecimal(transactionAmount, getApplication()),
                    transactionFreq))
        }
        return transactionData
    }

    fun getTransactionByDateAndCategoryAndCurrency(startDate: String, endDate: String,
                                                   currencyCode: String, accountName: String,
                                                   transactionType: String, categoryName: String?): MutableLiveData<Double>{
        isLoading.value = true
        var transactionAmount: Double = 0.toDouble()
        val data: MutableLiveData<Double> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionAmount = repository.getTransactionByDateAndCategoryAndCurrency(startDate, endDate,
                    currencyCode, accountName, transactionType, categoryName)
        }.invokeOnCompletion {
            isLoading.postValue(false)
            data.postValue(transactionAmount)
        }
        return data
    }

    fun getTransactionByDateAndBudgetAndCurrency(startDate: String, endDate: String,
                                                  currencyCode: String, accountName: String,
                                                  transactionType: String, budgetName: String?): MutableLiveData<Double>{
        isLoading.value = true
        var transactionAmount: Double = 0.toDouble()
        val data: MutableLiveData<Double> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionAmount = repository.getTransactionByDateAndBudgetAndCurrency(startDate, endDate,
                    currencyCode, accountName, transactionType, budgetName)
        }.invokeOnCompletion {
            isLoading.postValue(false)
            data.postValue(transactionAmount)
        }
        return data
    }

    fun getTransactionByDateAndBudgetAndCurrency(startDate: String, endDate: String,
                                                 currencyCode: String,
                                                 transactionType: String, budgetName: String?): MutableLiveData<Double>{
        isLoading.value = true
        var transactionAmount: Double = 0.toDouble()
        val data: MutableLiveData<Double> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            transactionAmount = repository.getTransactionByDateAndBudgetAndCurrency(startDate, endDate,
                    currencyCode, transactionType, budgetName)
        }.invokeOnCompletion {
            isLoading.postValue(false)
            data.postValue(transactionAmount)
        }
        return data
    }

    fun getTransactionListByDateAndAccount(startDate: String, endDate: String,
                                            accountName: String): MutableLiveData<MutableList<Transactions>>{
        val transactionData: MutableLiveData<MutableList<Transactions>> = MutableLiveData()
        var data: MutableList<Transactions> = arrayListOf()
        viewModelScope.launch(Dispatchers.IO) {
            data = repository.getTransactionListByDateAndAccount(startDate, endDate, accountName)
        }.invokeOnCompletion {
            transactionData.postValue(data)
        }
        return transactionData
    }

    fun addTransaction(type: String, description: String,
                       date: String, piggyBankName: String?, amount: String,
                       sourceName: String?, destinationName: String?, currencyName: String,
                       category: String?, tags: String?, budgetName: String?, fileUri: Uri?): LiveData<ApiResponses<TransactionSuccessModel>>{
        val transaction: MutableLiveData<ApiResponses<TransactionSuccessModel>> = MutableLiveData()
        val apiResponse: MediatorLiveData<ApiResponses<TransactionSuccessModel>> = MediatorLiveData()
        transactionService?.addTransaction(convertString(type),description, date ,piggyBankName,
                amount.replace(',', '.'),sourceName,destinationName,currencyName, category, tags, budgetName)?.enqueue(retrofitCallback({ response ->
            val errorBody = response.errorBody()
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
            if (response.isSuccessful) {
                var transactionJournalId = 0L
                viewModelScope.launch(Dispatchers.IO){
                    response.body()?.data?.transactionAttributes?.transactions?.forEachIndexed { _, transaction ->
                        transactionJournalId = response.body()?.data?.transactionId ?: 0
                        repository.insertTransaction(transaction)
                        repository.insertTransaction(TransactionIndex(response.body()?.data?.transactionId,
                                transaction.transaction_journal_id))
                    }
                }.invokeOnCompletion {
                    if(fileUri != null){
                        AttachmentWorker.initWorker(fileUri, transactionJournalId, getApplication())
                    }
                }
                transaction.postValue(ApiResponses(response.body()))
            } else {
                transaction.postValue(ApiResponses(errorBodyMessage))
            }
        })
        { throwable -> transaction.value = ApiResponses(throwable) })
        apiResponse.addSource(transaction) { apiResponse.value = it }
        return apiResponse
    }

    fun updateTransaction(transactionJournalId: Long, type: String, description: String,
                       date: String, amount: String,
                       sourceName: String?, destinationName: String?, currencyName: String,
                       category: String?, tags: String?, budgetName: String?, fileUri: Uri?): LiveData<ApiResponses<TransactionSuccessModel>>{
        val transaction: MutableLiveData<ApiResponses<TransactionSuccessModel>> = MutableLiveData()
        val apiResponse: MediatorLiveData<ApiResponses<TransactionSuccessModel>> = MediatorLiveData()
        var transactionId = 0L
        viewModelScope.launch(Dispatchers.IO){
            transactionId = repository.getTransactionIdFromJournalId(transactionJournalId)
        }.invokeOnCompletion {
            transactionService?.updateTransaction(transactionId, convertString(type), description, date,
                    amount.replace(',', '.'), sourceName, destinationName, currencyName, category, tags, budgetName)?.enqueue(retrofitCallback({ response ->
                val errorBody = response.errorBody()
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
                if (response.isSuccessful) {
                    viewModelScope.launch(Dispatchers.IO) {
                        viewModelScope.launch(Dispatchers.IO){
                            response.body()?.data?.transactionAttributes?.transactions?.forEachIndexed { _, transaction ->
                                repository.insertTransaction(transaction)
                                repository.insertTransaction(TransactionIndex(response.body()?.data?.transactionId,
                                        transaction.transaction_journal_id))
                            }
                        }
                    }.invokeOnCompletion {
                        if(fileUri != null){
                            AttachmentWorker.initWorker(fileUri, transactionJournalId, getApplication())
                        }
                    }
                    transaction.postValue(ApiResponses(response.body()))
                } else {
                    transaction.postValue(ApiResponses(errorBodyMessage))
                }
            })
            { throwable -> transaction.value = ApiResponses(throwable) })
        }
        apiResponse.addSource(transaction) { apiResponse.value = it }
        return apiResponse
    }

    fun getTransactionByJournalId(transactionJournalId: Long): LiveData<MutableList<Transactions>>{
        val transactionData: MutableLiveData<MutableList<Transactions>> = MutableLiveData()
        var data: MutableList<Transactions> = arrayListOf()
        viewModelScope.launch(Dispatchers.IO){
            data = repository.getTransactionByJournalId(transactionJournalId)
        }.invokeOnCompletion {
            transactionData.postValue(data)
        }
        return transactionData
    }

    fun deleteTransaction(transactionJournalId: Long): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        isLoading.value = true
        var isItDeleted = 0
        var transactionId = 0L
        viewModelScope.launch(Dispatchers.IO) {
            transactionId = repository.getTransactionIdFromJournalId(transactionJournalId)
            if(transactionId != 0L){
                isItDeleted = repository.deleteTransactionById(transactionId)
            }
        }.invokeOnCompletion {
            when (isItDeleted) {
                HttpConstants.FAILED -> {
                    isDeleted.postValue(false)
                    DeleteTransactionWorker.setupWorker(transactionId, getApplication())
                }
                HttpConstants.UNAUTHORISED -> {
                    isDeleted.postValue(false)
                }
                HttpConstants.NO_CONTENT_SUCCESS -> {
                    isDeleted.postValue(true)
                }
            }
            isLoading.postValue(false)
        }
        return isDeleted
    }

    private fun convertString(type: String) = type.substring(0,1).toLowerCase() + type.substring(1).toLowerCase()

    fun getTransactionAttachment(journalId: Long): MutableLiveData<MutableList<AttachmentData>>{
        isLoading.value = true
        val attachmentRepository = AttachmentRepository(AppDatabase.getInstance(getApplication()).attachmentDataDao())
        val data: MutableLiveData<MutableList<AttachmentData>> = MutableLiveData()
        var attachmentData: MutableList<AttachmentData> = arrayListOf()
        var transactionId = 0L
        viewModelScope.launch(Dispatchers.IO) {
            transactionId = repository.getTransactionIdFromJournalId(journalId)
        }.invokeOnCompletion {
            transactionService?.getTransactionAttachment(transactionId)?.enqueue(retrofitCallback({ response ->
                if (response.isSuccessful) {
                    response.body()?.data?.forEachIndexed { _, attachmentData ->
                        viewModelScope.launch(Dispatchers.IO) {
                            attachmentRepository.insertAttachmentInfo(attachmentData)
                        }
                    }
                    data.postValue(response.body()?.data)
                    isLoading.value = false
                } else {
                    /** 7 March 2019
                     * In an ideal world, we should be using foreign keys and relationship to
                     * retrieve related attachments by transaction ID. but alas! the world we live in
                     * isn't ideal, therefore we have to develop a hack.
                     *
                     * P.S. This was a bad database design mistake I made when I wrote this software. On
                     * hindsight I should have looked at James Cole's design schema. But hindsight 10/10
                     **/
                    viewModelScope.launch(Dispatchers.IO) {
                        attachmentData = attachmentRepository.getAttachmentFromJournalId(journalId)
                    }.invokeOnCompletion {
                        isLoading.postValue(false)
                        data.postValue(attachmentData)
                    }
                }
            })
            { throwable ->
                viewModelScope.launch(Dispatchers.IO) {
                    attachmentData = attachmentRepository.getAttachmentFromJournalId(journalId)
                }.invokeOnCompletion {
                    isLoading.postValue(false)
                    data.postValue(attachmentData)
                }
                apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage))
            })
        }
        return data
    }

    fun getTransactionListByDateAndBudget(startDate: String, endDate: String, budgetName: String?): LiveData<MutableList<Transactions>>{
        val transactionData: MutableLiveData<MutableList<Transactions>> = MutableLiveData()
        var data: MutableList<Transactions> = arrayListOf()
        viewModelScope.launch(Dispatchers.IO) {
            data = repository.getTransactionListByDateAndBudget(startDate, endDate, budgetName)
        }.invokeOnCompletion {
            transactionData.postValue(data)
        }
        return transactionData
    }

    fun getTransactionByDescription(query: String) : LiveData<List<String>>{
        val transactionData: MutableLiveData<List<String>> = MutableLiveData()
        val displayName = arrayListOf<String>()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getTransactionByDescription(query)
                    .distinctUntilChanged()
                    .debounce(1000)
                    .collectLatest { transactionList ->
                        transactionList.forEach { transactions ->
                            displayName.add(transactions.description)
                        }
                        transactionData.postValue(displayName.distinct())
            }
        }
        return transactionData
    }

}