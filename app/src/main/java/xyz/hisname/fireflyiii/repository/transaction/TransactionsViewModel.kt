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
import xyz.hisname.fireflyiii.data.remote.firefly.api.AttachmentService
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
    private val transactionService by lazy { genericService().create(TransactionService::class.java) }
    private val transactionDataDao = AppDatabase.getInstance(application).transactionDataDao()

    init {
        repository = TransactionRepository(transactionDataDao, transactionService)
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

    fun getTransactionAttachment(journalId: Long): MutableLiveData<MutableList<AttachmentData>>{
        isLoading.value = true
        val attachmentRepository = AttachmentRepository(AppDatabase.getInstance(getApplication()).attachmentDataDao(),
                genericService().create(AttachmentService::class.java))
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
}