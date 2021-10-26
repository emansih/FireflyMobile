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

package xyz.hisname.fireflyiii.ui.transaction.details

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.attachment.AttachableType
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.extension.downloadFile
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.AttachmentWorker
import xyz.hisname.fireflyiii.workers.transaction.DeleteTransactionWorker
import java.io.File

class TransactionDetailsViewModel(application: Application): BaseViewModel(application) {

    private val transactionService = genericService().create(TransactionService::class.java)
    private val transactionDao = AppDatabase.getInstance(application, getUniqueHash()).transactionDataDao()
    private val transactionRepository = TransactionRepository(transactionDao, transactionService)
    private val attachmentDao =  AppDatabase.getInstance(getApplication(), getUniqueHash()).attachmentDataDao()
    val transactionAttachment = MutableLiveData<List<AttachmentData>>()

    fun duplicationTransactionByJournalId(journalId: Long, fileUri: List<Uri>): MutableLiveData<String>{
        val message = MutableLiveData<String>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            val addTransaction = transactionRepository.duplicationTransactionByJournalId(journalId)
            when {
                addTransaction.response != null -> {
                    if(fileUri.isNotEmpty()){
                        addTransaction.response.data.transactionAttributes.transactions.forEach { transactions ->
                            AttachmentWorker.initWorker(fileUri, transactions.transaction_journal_id,
                                getApplication(), AttachableType.TRANSACTION)
                        }
                    }
                    message.postValue("Duplicate success!")
                }
                addTransaction.errorMessage != null -> {
                    message.postValue(addTransaction.errorMessage.toString())
                }
                addTransaction.error != null -> {
                    message.postValue(addTransaction.error.localizedMessage)
                }
                else -> {
                    message.postValue("Failed to duplicate transaction")
                }
            }
            isLoading.postValue(false)
        }
        return message
    }

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
        getApplication<Application>().downloadFile(newManager.accessToken, attachmentData, fileToOpen)
        getApplication<Application>().registerReceiver(object : BroadcastReceiver(){
            override fun onReceive(context: Context, intent: Intent) {
                downloadedFile.postValue(fileToOpen)
            }
        }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        return downloadedFile
    }

}