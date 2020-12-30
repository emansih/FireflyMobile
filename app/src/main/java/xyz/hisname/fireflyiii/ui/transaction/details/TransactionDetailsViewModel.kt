package xyz.hisname.fireflyiii.ui.transaction.details

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.extension.downloadFile
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.transaction.DeleteTransactionWorker
import java.io.File

class TransactionDetailsViewModel(application: Application): BaseViewModel(application) {

    private val transactionService = genericService().create(TransactionService::class.java)
    private val transactionDao = AppDatabase.getInstance(application).transactionDataDao()
    private val transactionRepository = TransactionRepository(transactionDao, transactionService)
    private val attachmentDao =  AppDatabase.getInstance(getApplication()).attachmentDataDao()
    val transactionAttachment = MutableLiveData<List<AttachmentData>>()

    fun getTransactionByJournalId(journalId: Long): LiveData<Transactions>{
        val transactionData = MutableLiveData<Transactions>()
        viewModelScope.launch(Dispatchers.IO){
            transactionData.postValue(transactionRepository.getTransactionByJournalId(journalId))
            transactionAttachment.postValue(transactionRepository.getAttachment(journalId,
                    attachmentDao))
        }
        return transactionData
    }

    fun deleteTransaction(journalId: Long): MutableLiveData<Boolean>{
        isLoading.postValue(true)
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            val transactionId = transactionRepository.getTransactionIdFromJournalId(journalId)
            transactionRepository.deleteTransactionById(transactionId)
            if(transactionId != 0L){
                when (transactionRepository.deleteTransactionById(transactionId)) {
                    HttpConstants.FAILED -> {
                        isDeleted.postValue(false)
                        DeleteTransactionWorker.setupWorker(journalId, getApplication())
                    }
                    HttpConstants.UNAUTHORISED -> {
                        isDeleted.postValue(false)
                    }
                    HttpConstants.NO_CONTENT_SUCCESS -> {
                        isDeleted.postValue(true)
                    }
                }
            }
        }
        return isDeleted
    }

    fun downloadAttachment(attachmentData: AttachmentData): LiveData<File>{
        val downloadedFile: MutableLiveData<File> = MutableLiveData()
        val fileName = attachmentData.attachmentAttributes.filename
        val fileToOpen = File(getApplication<Application>().getExternalFilesDir(null).toString() +
                File.separator + fileName)
        getApplication<Application>().downloadFile(accManager.accessToken, attachmentData, fileToOpen)
        getApplication<Application>().registerReceiver(object : BroadcastReceiver(){
            override fun onReceive(context: Context, intent: Intent) {
                downloadedFile.postValue(fileToOpen)
            }
        }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        return downloadedFile
    }

}