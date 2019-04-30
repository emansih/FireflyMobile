package xyz.hisname.fireflyiii.repository.transaction

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.attachment.AttachmentRepository
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionSuccessModel
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.LocaleNumberParser
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import java.math.BigDecimal
import kotlin.math.absoluteValue

class TransactionsViewModel(application: Application): BaseViewModel(application) {

    val repository: TransactionRepository
    private val transactionService by lazy { genericService()?.create(TransactionService::class.java) }

    init {
        val transactionDataDao = AppDatabase.getInstance(application).transactionDataDao()
        repository = TransactionRepository(transactionDataDao)
    }

    fun getAllData(startDate: String?, endDate: String?): LiveData<MutableList<TransactionData>>  {
        loadRemoteData(startDate, endDate, "all")
        return repository.allTransaction
    }

    fun getWithdrawalList(startDate: String?, endDate: String?) = loadRemoteData(startDate, endDate, "Withdrawal")

    fun getDepositList(startDate: String?, endDate: String?) = loadRemoteData(startDate, endDate, "Deposit")

    fun getTransferList(startDate: String?, endDate: String?) = loadRemoteData(startDate, endDate, "Transfer")

    fun getRecentTransaction(limit: Int): LiveData<MutableList<TransactionData>>{
        isLoading.value = true
        var recentData: MutableList<TransactionData> = arrayListOf()
        val data: MutableLiveData<MutableList<TransactionData>> = MutableLiveData()
        transactionService?.getAllTransactions("","", "all")?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val networkData = response.body()
                recentData = networkData?.data?.toMutableList() ?: arrayListOf()
                scope.launch(Dispatchers.IO){
                    networkData?.data?.forEachIndexed { _, transactionData ->
                        repository.insertTransaction(transactionData)
                    }
                }.invokeOnCompletion {
                    if(limit < networkData?.data?.size ?: 0){
                        data.postValue(recentData.take(limit).toMutableList())
                    } else {
                        data.postValue(recentData)
                    }
                    isLoading.postValue(false)
                }
            } else {
                scope.launch(Dispatchers.IO){
                    recentData = repository.recentTransactions(limit)
                }.invokeOnCompletion {
                    data.postValue(recentData)
                    isLoading.postValue(false)
                }
            }
        })
        { throwable ->
            apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage))
            scope.launch(Dispatchers.IO){
                recentData = repository.recentTransactions(limit)
            }.invokeOnCompletion {
                data.postValue(recentData)
                isLoading.postValue(false)
            }
        })
        return data
    }

    fun getWithdrawalAmountWithCurrencyCode(startDate: String, endDate: String, currencyCode: String): LiveData<Double>{
        isLoading.value = true
        var withdrawData: Double = 0.toDouble()
        val data: MutableLiveData<Double> = MutableLiveData()
        val transactionData: MutableList<TransactionData> = arrayListOf()
        transactionService?.getPaginatedTransactions(startDate, endDate, "withdrawal", 1)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val networkData = response.body()
                if (networkData != null) {
                    if(networkData.meta.pagination.current_page == networkData.meta.pagination.total_pages){
                        scope.launch(Dispatchers.IO){
                            repository.deleteTransactionsByDate(startDate, endDate, "Withdrawal")
                        }.invokeOnCompletion {
                            transactionData.addAll(networkData.data)
                            if(networkData.meta.pagination.total_pages > networkData.meta.pagination.current_page) {
                                for(items in 2..networkData.meta.pagination.total_pages){
                                    transactionService?.getPaginatedTransactions(startDate, endDate, "withdrawal", items)?.enqueue(retrofitCallback({ pagination ->
                                        pagination.body()?.data?.forEachIndexed{ _, transData ->
                                            transactionData.add(transData)
                                        }
                                    }))
                                }
                            }
                            scope.launch(Dispatchers.IO){
                                transactionData.forEachIndexed { _, transData ->
                                    repository.insertTransaction(transData)
                                }
                            }.invokeOnCompletion {
                                scope.launch(Dispatchers.IO) {
                                    withdrawData = repository.allWithdrawalWithCurrencyCode(startDate, endDate, currencyCode)
                                }.invokeOnCompletion {
                                    data.postValue(LocaleNumberParser.parseDecimal(withdrawData, getApplication()).absoluteValue)
                                    isLoading.postValue(false)
                                }
                            }
                        }
                    }
                }
            } else {
                val responseError = response.errorBody()
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    apiResponse.postValue(gson.message)
                }
                scope.launch(Dispatchers.IO) {
                    withdrawData = repository.allWithdrawalWithCurrencyCode(startDate, endDate, currencyCode)
                }.invokeOnCompletion {
                    data.postValue(LocaleNumberParser.parseDecimal(withdrawData, getApplication()).absoluteValue)
                    isLoading.postValue(false)
                }

            }
        })
        { throwable ->
            scope.launch(Dispatchers.IO) {
                withdrawData = repository.allWithdrawalWithCurrencyCode(startDate, endDate, currencyCode)
            }.invokeOnCompletion {
                data.postValue(LocaleNumberParser.parseDecimal(withdrawData, getApplication()).absoluteValue)
                isLoading.postValue(false)
            }
            apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage))
        })
        return data
    }

    fun getDepositAmountWithCurrencyCode(startDate: String, endDate: String, currencyCode: String): LiveData<Double>{
        isLoading.value = true
        var depositData: Double = 0.toDouble()
        val data: MutableLiveData<Double> = MutableLiveData()
        val transactionData: MutableList<TransactionData> = arrayListOf()
        transactionService?.getPaginatedTransactions(startDate, endDate, "deposit", 1)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val networkData = response.body()
                if (networkData != null) {
                    if(networkData.meta.pagination.current_page == networkData.meta.pagination.total_pages){
                        scope.launch(Dispatchers.IO){
                            repository.deleteTransactionsByDate(startDate, endDate, "Deposit")
                        }.invokeOnCompletion {
                            transactionData.addAll(networkData.data)
                            if(networkData.meta.pagination.total_pages > networkData.meta.pagination.current_page) {
                                for(items in 2..networkData.meta.pagination.total_pages){
                                    transactionService?.getPaginatedTransactions(startDate, endDate, "deposit", items)?.enqueue(retrofitCallback({ pagination ->
                                        pagination.body()?.data?.forEachIndexed{ _, transData ->
                                            transactionData.add(transData)
                                        }
                                    }))
                                }
                            }
                            scope.launch(Dispatchers.IO){
                                transactionData.forEachIndexed { _, transData ->
                                    repository.insertTransaction(transData)
                                }
                            }.invokeOnCompletion {
                                scope.launch(Dispatchers.IO) {
                                    depositData = repository.allDepositWithCurrencyCode(startDate, endDate, currencyCode)
                                }.invokeOnCompletion {
                                    data.postValue(LocaleNumberParser.parseDecimal(depositData, getApplication()).absoluteValue)
                                    isLoading.postValue(false)
                                }
                            }
                        }
                    }
                }
            } else {
                val responseError = response.errorBody()
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    apiResponse.postValue(gson.message)
                }
                scope.launch(Dispatchers.IO) {
                    depositData = repository.allDepositWithCurrencyCode(startDate, endDate, currencyCode)
                }.invokeOnCompletion {
                    data.postValue(LocaleNumberParser.parseDecimal(depositData, getApplication()).absoluteValue)
                    isLoading.postValue(false)
                }

            }
        })
        { throwable ->
            scope.launch(Dispatchers.IO) {
                depositData = repository.allDepositWithCurrencyCode(startDate, endDate, currencyCode)
            }.invokeOnCompletion {
                data.postValue(LocaleNumberParser.parseDecimal(depositData, getApplication()).absoluteValue)
                isLoading.postValue(false)
            }
            apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage))
        })
        return data
    }

    // My god.... the name of this function is sooooooo looong...
    fun getTransactionsByAccountAndCurrencyCodeAndDate(startDate: String, endDate: String,
                                                               currencyCode: String,
                                                               accountName: String): LiveData<BigDecimal>{
        isLoading.value = true
        var transactionAmount: BigDecimal = 0.toBigDecimal()
        var transactionData: MutableList<TransactionData> = arrayListOf()
        val data: MutableLiveData<BigDecimal> = MutableLiveData()
        loadRemoteData(startDate, endDate, "all")
        scope.launch(Dispatchers.IO){
            transactionData = repository.getTransactionsByAccountAndCurrencyCodeAndDate(startDate, endDate, currencyCode, accountName)
        }.invokeOnCompletion {
            isLoading.postValue(false)
            transactionData.forEachIndexed { _, transactionData ->
                transactionAmount = transactionAmount.add(transactionData.transactionAttributes?.amount?.toBigDecimal()?.abs())
            }
            data.postValue(transactionAmount)
        }
        return data
    }

    fun getUniqueCategoryByDate(startDate: String, endDate: String, currencyCode: String,
                                sourceName: String, transactionType: String): MutableLiveData<MutableList<String>>{
        isLoading.value = true
        var transactionData: MutableList<String> = arrayListOf()
        val data: MutableLiveData<MutableList<String>> = MutableLiveData()
        scope.launch(Dispatchers.IO){
            transactionData = repository.getUniqueCategoryByDate(startDate, endDate, currencyCode, sourceName, transactionType)
        }.invokeOnCompletion {
            data.postValue(transactionData)
            isLoading.postValue(false)
        }
        return data
    }

    fun getUniqueBudgetByDate(startDate: String, endDate: String, currencyCode: String,
                              sourceName: String, transactionType: String): MutableLiveData<MutableList<String>>{
        isLoading.value = true
        var transactionData: MutableList<String> = arrayListOf()
        val data: MutableLiveData<MutableList<String>> = MutableLiveData()
        scope.launch(Dispatchers.IO){
            transactionData = repository.getUniqueBudgetByDate(startDate, endDate, currencyCode, sourceName, transactionType)
        }.invokeOnCompletion {
            data.postValue(transactionData)
            isLoading.postValue(false)
        }
        return data
    }

    fun getUniqueBudgetByDate(startDate: String, endDate: String, currencyCode: String,
                              transactionType: String): MutableLiveData<MutableList<String>>{
        isLoading.value = true
        var transactionData: MutableList<String> = arrayListOf()
        val data: MutableLiveData<MutableList<String>> = MutableLiveData()
        scope.launch(Dispatchers.IO){
            transactionData = repository.getUniqueBudgetByDate(startDate, endDate, currencyCode, transactionType)
        }.invokeOnCompletion {
            data.postValue(transactionData)
            isLoading.postValue(false)
        }
        return data
    }

    fun getTotalTransactionAmountByDateAndCurrency(startDate: String, endDate: String,
                                             currencyCode: String, accountName: String,
                                                   transactionType: String): MutableLiveData<Double>{
        isLoading.value = true
        var transactionAmount: Double = 0.toDouble()
        val data: MutableLiveData<Double> = MutableLiveData()
        scope.launch(Dispatchers.IO){
            transactionAmount = repository.getTotalTransactionType(startDate, endDate,
                    currencyCode, accountName, transactionType)
        }.invokeOnCompletion {
            isLoading.postValue(false)
            data.postValue(transactionAmount)
        }
        return data
    }

    fun getTotalTransactionAmountByDateAndCurrency(startDate: String, endDate: String,
                                                   currencyCode: String,
                                                   transactionType: String): MutableLiveData<Double>{
        isLoading.value = true
        var transactionAmount = 0.0
        val data: MutableLiveData<Double> = MutableLiveData()
        scope.launch(Dispatchers.IO){
            transactionAmount = repository.getTotalTransactionType(startDate, endDate,
                    currencyCode, transactionType)
        }.invokeOnCompletion {
            isLoading.postValue(false)
            data.postValue(transactionAmount)
        }
        return data
    }

    fun getTransactionByDateAndCategoryAndCurrency(startDate: String, endDate: String,
                                                   currencyCode: String, accountName: String,
                                                   transactionType: String, categoryName: String?): MutableLiveData<Double>{
        isLoading.value = true
        var transactionAmount: Double = 0.toDouble()
        val data: MutableLiveData<Double> = MutableLiveData()
        scope.launch(Dispatchers.IO){
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
        scope.launch(Dispatchers.IO){
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
        scope.launch(Dispatchers.IO){
            transactionAmount = repository.getTransactionByDateAndBudgetAndCurrency(startDate, endDate,
                    currencyCode, transactionType, budgetName)
        }.invokeOnCompletion {
            isLoading.postValue(false)
            data.postValue(transactionAmount)
        }
        return data
    }

    fun getTransactionListByDateAndAccount(startDate: String, endDate: String,
                                            accountName: String): MutableLiveData<MutableList<TransactionData>>{
        val transactionData: MutableLiveData<MutableList<TransactionData>> = MutableLiveData()
        var data: MutableList<TransactionData> = arrayListOf()
        scope.async(Dispatchers.IO) {
            data = repository.getTransactionListByDateAndAccount(startDate, endDate, accountName)
        }.invokeOnCompletion {
            transactionData.postValue(data)
        }
        return transactionData
    }

    fun addTransaction(type: String, description: String,
                       date: String, piggyBankName: String?, billName: String?, amount: String,
                       sourceName: String?, destinationName: String?, currencyName: String,
                       category: String?, tags: String?, budgetName: String?): LiveData<ApiResponses<TransactionSuccessModel>>{
        val transaction: MutableLiveData<ApiResponses<TransactionSuccessModel>> = MutableLiveData()
        val apiResponse: MediatorLiveData<ApiResponses<TransactionSuccessModel>> = MediatorLiveData()
        transactionService?.addTransaction(convertString(type),description,date,piggyBankName,billName,
                amount,sourceName,destinationName,currencyName, category, tags, budgetName)?.enqueue(retrofitCallback({ response ->
            val errorBody = response.errorBody()
            var errorBodyMessage = ""
            if (errorBody != null) {
                errorBodyMessage = String(errorBody.bytes())
                val gson = Gson().fromJson(errorBodyMessage, ErrorModel::class.java)
                errorBodyMessage = when {
                    gson.errors.transactions_currency != null -> "Currency Code Required"
                    gson.errors.bill_name != null -> "Invalid Bill Name"
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
                response.body()?.data?.forEachIndexed { _, transaction ->
                    scope.launch(Dispatchers.IO) { repository.insertTransaction(transaction) }
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

    fun updateTransaction(transactionId: Long, type: String, description: String,
                       date: String, billName: String?, amount: String,
                       sourceName: String?, destinationName: String?, currencyName: String,
                       category: String?, tags: String?, budgetName: String?): LiveData<ApiResponses<TransactionSuccessModel>>{
        val transaction: MutableLiveData<ApiResponses<TransactionSuccessModel>> = MutableLiveData()
        val apiResponse: MediatorLiveData<ApiResponses<TransactionSuccessModel>> = MediatorLiveData()
        transactionService?.updateTransaction(transactionId, convertString(type),description,date,billName,
                amount,sourceName,destinationName,currencyName, category, tags, budgetName)?.enqueue(retrofitCallback({ response ->
            val errorBody = response.errorBody()
            var errorBodyMessage = ""
            if (errorBody != null) {
                errorBodyMessage = String(errorBody.bytes())
                val gson = Gson().fromJson(errorBodyMessage, ErrorModel::class.java)
                errorBodyMessage = when {
                    gson.errors.transactions_currency != null -> "Currency Code Required"
                    gson.errors.bill_name != null -> "Invalid Bill Name"
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
                response.body()?.data?.forEachIndexed { _, transaction ->
                    scope.launch(Dispatchers.IO) {
                        repository.insertTransaction(transaction)
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

    fun getTransactionById(transactionId: Long): LiveData<MutableList<TransactionData>>{
        val transactionData: MutableLiveData<MutableList<TransactionData>> = MutableLiveData()
        var data: MutableList<TransactionData> = arrayListOf()
        scope.async(Dispatchers.IO) {
            data = repository.getTransactionById(transactionId)
        }.invokeOnCompletion {
            transactionData.postValue(data)
        }
        return transactionData
    }

    fun deleteTransaction(transactionId: Long): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        isLoading.value = true
        transactionService?.deleteTransactionById(transactionId)?.enqueue(retrofitCallback({ response ->
            if (response.code() == 204 || response.code() == 200) {
                scope.async(Dispatchers.IO) {
                    repository.deleteTransactionById(transactionId)
                }.invokeOnCompletion {
                    isDeleted.postValue(true)
                }
            }else {
                isDeleted.postValue(false)
            }
        })
        { throwable ->
            isDeleted.postValue(false)
        })
        isLoading.value = false
        return isDeleted
    }

    private fun convertString(type: String) = type.substring(0,1).toLowerCase() + type.substring(1).toLowerCase()

    private fun loadRemoteData(startDate: String?, endDate: String?, source: String): LiveData<MutableList<TransactionData>>{
        var transactionData: MutableList<TransactionData> = arrayListOf()
        val data: MutableLiveData<MutableList<TransactionData>> = MutableLiveData()
        transactionService?.getPaginatedTransactions(startDate, endDate, source, 1)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val networkData = response.body()
                if (networkData != null) {
                    if(networkData.meta.pagination.current_page == networkData.meta.pagination.total_pages){
                        scope.launch(Dispatchers.IO){
                            repository.deleteTransactionsByDate(startDate, endDate, source)
                        }.invokeOnCompletion {
                            transactionData.addAll(networkData.data)
                            networkData.data.forEachIndexed{ _, transactionData ->
                                scope.launch(Dispatchers.IO) { repository.insertTransaction(transactionData) }
                            }
                            if(networkData.meta.pagination.total_pages > networkData.meta.pagination.current_page) {
                                for(items in 2..networkData.meta.pagination.total_pages){
                                    transactionService?.getPaginatedTransactions(startDate, endDate, source, items)?.enqueue(retrofitCallback({ pagination ->
                                        pagination.body()?.data?.forEachIndexed{ _, transData ->
                                            transactionData.add(transData)
                                        }
                                    }))
                                }
                            }
                            transactionData.forEachIndexed{ _, transData ->
                                scope.launch(Dispatchers.IO) {
                                    repository.insertTransaction(transData)
                                }
                            }
                            data.postValue(transactionData.toMutableList())
                        }
                    }
                }
            } else {
                val responseError = response.errorBody()
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    apiResponse.postValue(gson.message)
                }
                scope.launch(Dispatchers.IO) {
                    transactionData = repository.transactionList(startDate, endDate, source)
                }.invokeOnCompletion {
                    data.postValue(transactionData)
                }
            }
        })
        { throwable ->
            scope.launch(Dispatchers.IO) {
                transactionData = repository.transactionList(startDate, endDate, source)
            }
            apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage))
        })
        return data
    }

    fun getTransactionAttachment(transactionId: Long, journalId: Long): MutableLiveData<MutableList<AttachmentData>>{
        isLoading.value = true
        val attachmentRepository = AttachmentRepository(AppDatabase.getInstance(getApplication()).attachmentDataDao())
        val data: MutableLiveData<MutableList<AttachmentData>> = MutableLiveData()
        var attachmentData: MutableList<AttachmentData> = arrayListOf()
        transactionService?.getTransactionAttachment(transactionId)?.enqueue(retrofitCallback({ response ->
            if(response.isSuccessful){
                response.body()?.data?.forEachIndexed { _, attachmentData ->
                    scope.launch(Dispatchers.IO){
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
                scope.launch(Dispatchers.IO){
                    attachmentData = attachmentRepository.getAttachmentFromJournalId(journalId)
                }.invokeOnCompletion {
                    isLoading.postValue(false)
                    data.postValue(attachmentData)
                }
            }
        })
        { throwable ->
            scope.launch(Dispatchers.IO){
                attachmentData = attachmentRepository.getAttachmentFromJournalId(journalId)
            }.invokeOnCompletion {
                isLoading.postValue(false)
                data.postValue(attachmentData)
            }
            apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage))
        })
        return data
    }

    fun getTransactionListByDateAndBudget(startDate: String, endDate: String, budgetName: String): LiveData<MutableList<TransactionData>>{
        val transactionData: MutableLiveData<MutableList<TransactionData>> = MutableLiveData()
        var data: MutableList<TransactionData> = arrayListOf()
        scope.async(Dispatchers.IO) {
            data = repository.getTransactionListByDateAndBudget(startDate, endDate, budgetName)
        }.invokeOnCompletion {
            transactionData.postValue(data)
        }
        return transactionData
    }

}