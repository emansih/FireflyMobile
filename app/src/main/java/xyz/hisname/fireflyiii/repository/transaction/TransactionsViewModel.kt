package xyz.hisname.fireflyiii.repository.transaction

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineExceptionHandler
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
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.workers.transaction.AttachmentWorker
import xyz.hisname.fireflyiii.workers.transaction.DeleteTransactionWorker
import xyz.hisname.fireflyiii.workers.transaction.TransactionWorker

class TransactionsViewModel(application: Application): BaseViewModel(application) {

    val repository: TransactionRepository
    val transactionAmount: MutableLiveData<String> = MutableLiveData()
    private val transactionService by lazy { genericService()?.create(TransactionService::class.java) }
    private val transactionDataDao = AppDatabase.getInstance(application).transactionDataDao()

    init {
        repository = TransactionRepository(transactionDataDao, transactionService)
    }

    fun addTransaction(type: String, description: String,
                       date: String, piggyBankName: String?, amount: String,
                       sourceName: String?, destinationName: String?, currencyName: String,
                       category: String?, tags: String?, budgetName: String?,
                       fileUri: ArrayList<Uri>, notes: String): LiveData<ApiResponses<TransactionSuccessModel>>{
        val transaction: MutableLiveData<ApiResponses<TransactionSuccessModel>> = MutableLiveData()
        val apiResponse: MediatorLiveData<ApiResponses<TransactionSuccessModel>> = MediatorLiveData()
        transactionService?.addTransaction(convertString(type),description, date ,piggyBankName,
                amount.replace(',', '.'),sourceName,destinationName,currencyName,
                category, tags, budgetName, notes)?.enqueue(retrofitCallback({ response ->
            val errorBody = response.errorBody()
            var errorBodyMessage = ""
            if (errorBody != null) {
                errorBodyMessage = String(errorBody.bytes())
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorBodyMessage)
                try {
                    moshi?.errors?.transactions_currency?.let {
                        errorBodyMessage = moshi.errors.transactions_currency[0]
                    }
                    moshi?.errors?.piggy_bank_name?.let {
                        errorBodyMessage = moshi.errors.piggy_bank_name[0]
                    }
                    moshi?.errors?.transactions_destination_name?.let {
                        errorBodyMessage = moshi.errors.transactions_destination_name[0]
                    }
                    moshi?.errors?.transactions_source_name?.let {
                        errorBodyMessage = moshi.errors.transactions_source_name[0]
                    }
                    moshi?.errors?.transaction_destination_id?.let {
                        errorBodyMessage = moshi.errors.transaction_destination_id[0]
                    }
                    moshi?.errors?.transaction_amount?.let {
                        errorBodyMessage = "Amount field is required"
                    }
                    moshi?.errors?.description?.let {
                        errorBodyMessage = moshi.errors.description[0]
                    }
                } catch (exception: Exception){
                    errorBodyMessage = "The given data was invalid"
                }
            }
            if (response.isSuccessful) {
                var transactionJournalId = 0L
                viewModelScope.launch(Dispatchers.IO){
                    response.body()?.data?.transactionAttributes?.transactions?.forEachIndexed { _, transaction ->
                        transactionJournalId = transaction.transaction_journal_id
                        repository.insertTransaction(transaction)
                        repository.insertTransaction(TransactionIndex(response.body()?.data?.transactionId,
                                transaction.transaction_journal_id))
                    }
                }.invokeOnCompletion {
                    if(fileUri.isNotEmpty()){
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
                          category: String?, tags: String?, budgetName: String?,
                          fileUri: ArrayList<Uri>, notes: String): LiveData<ApiResponses<TransactionSuccessModel>>{
        val transaction: MutableLiveData<ApiResponses<TransactionSuccessModel>> = MutableLiveData()
        val apiResponse: MediatorLiveData<ApiResponses<TransactionSuccessModel>> = MediatorLiveData()
        var transactionId = 0L
        viewModelScope.launch(Dispatchers.IO){
            transactionId = repository.getTransactionIdFromJournalId(transactionJournalId)
        }.invokeOnCompletion {
            transactionService?.updateTransaction(transactionId, convertString(type), description, date,
                    amount.replace(',', '.'), sourceName, destinationName, currencyName,
                    category, tags, budgetName, notes)?.enqueue(retrofitCallback({ response ->
                val errorBody = response.errorBody()
                var errorBodyMessage = ""
                if (errorBody != null) {
                    errorBodyMessage = String(errorBody.bytes())
                    val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorBodyMessage)
                    try {
                        moshi?.errors?.transactions_currency?.let {
                            errorBodyMessage = moshi.errors.transactions_currency[0]
                        }
                        moshi?.errors?.piggy_bank_name?.let {
                            errorBodyMessage = moshi.errors.piggy_bank_name[0]
                        }
                        moshi?.errors?.transactions_destination_name?.let {
                            errorBodyMessage = moshi.errors.transactions_destination_name[0]
                        }
                        moshi?.errors?.transactions_source_name?.let {
                            errorBodyMessage = moshi.errors.transactions_source_name[0]
                        }
                        moshi?.errors?.transaction_destination_id?.let {
                            errorBodyMessage = moshi.errors.transaction_destination_id[0]
                        }
                        moshi?.errors?.transaction_amount?.let {
                            errorBodyMessage = "Amount field is required"
                        }
                        moshi?.errors?.description?.let {
                            errorBodyMessage = moshi.errors.description[0]
                        }
                    } catch (exception: Exception){
                        errorBodyMessage = "The given data was invalid"
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
                        if(fileUri.isNotEmpty()){
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
            if(transactionId == 0L){
                // User is offline and transaction is pending. Cancel work
                TransactionWorker.cancelWorker(transactionJournalId, getApplication())
            }
            isItDeleted = repository.deleteTransactionById(transactionId)
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
                        try {
                            attachmentData = attachmentRepository.getAttachmentFromJournalId(journalId)
                            isLoading.postValue(false)
                            data.postValue(attachmentData)
                        } catch (exception: Exception){ }
                    }
                }
            })
            { throwable ->
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        attachmentData = attachmentRepository.getAttachmentFromJournalId(journalId)
                        isLoading.postValue(false)
                        data.postValue(attachmentData)
                    } catch (exception: Exception){ }

                }
                apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage))
            })
        }
        return data
    }

    fun getTransactionByDescription(query: String) : LiveData<List<String>>{
        val transactionData: MutableLiveData<List<String>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getTransactionByDescription(query)
                    .collectLatest { transactionList ->
                        transactionData.postValue(transactionList.distinct())
            }
        }
        return transactionData
    }

}