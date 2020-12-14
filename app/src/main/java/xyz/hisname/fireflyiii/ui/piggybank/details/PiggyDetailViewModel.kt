package xyz.hisname.fireflyiii.ui.piggybank.details

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.AttachmentService
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.attachment.AttachmentRepository
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.piggybank.PiggyRepository
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.piggybank.DeletePiggyWorker
import java.io.File

class PiggyDetailViewModel(application: Application): BaseViewModel(application) {

    private val piggyRepository = PiggyRepository(
            AppDatabase.getInstance(application).piggyDataDao(),
            genericService().create(PiggybankService::class.java)
    )

    private val attachmentDao = AppDatabase.getInstance(getApplication()).attachmentDataDao()

    var accountId: Long = 0
        private set

    var accountName: String = ""
        private set
    val piggyAttachment = MutableLiveData<List<AttachmentData>>()

    fun getPiggyBankById(piggyBankId: Long): LiveData<PiggyData>{
        val piggyLiveData = MutableLiveData<PiggyData>()
        viewModelScope.launch(Dispatchers.IO){
            val piggyData = piggyRepository.getPiggyById(piggyBankId)
            accountId = piggyData.piggyAttributes.account_id ?: 0
            accountName = piggyData.piggyAttributes.account_name ?: ""
            piggyLiveData.postValue(piggyData)
            piggyAttachment.postValue(piggyRepository.getAttachment(piggyBankId, attachmentDao))
        }
        return piggyLiveData
    }

    fun deletePiggyBank(piggyId: Long): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            isLoading.postValue(true)
            when (piggyRepository.deletePiggyById(piggyId)) {
                HttpConstants.FAILED -> {
                    isDeleted.postValue(false)
                    DeletePiggyWorker.initPeriodicWorker(piggyId, getApplication())
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

    fun downloadAttachment(attachmentData: AttachmentData): LiveData<File>{
        isLoading.postValue(true)
        val fileName = attachmentData.attachmentAttributes.filename
        val fileToOpen = File(getApplication<Application>().getExternalFilesDir(null).toString() +
                File.separator + fileName)
        val downloadedFile: MutableLiveData<File> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            val attachmentRepository = AttachmentRepository(attachmentDao,
                    genericService().create(AttachmentService::class.java))
            downloadedFile.postValue(attachmentRepository.downloadOrOpenAttachment(
                    attachmentData.attachmentAttributes.download_uri, fileToOpen))
            isLoading.postValue(false)
        }
        return downloadedFile
    }
}