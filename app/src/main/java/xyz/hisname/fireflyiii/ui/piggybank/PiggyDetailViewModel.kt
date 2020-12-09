package xyz.hisname.fireflyiii.ui.piggybank

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.piggybank.PiggyRepository
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.piggybank.DeletePiggyWorker

class PiggyDetailViewModel(application: Application): BaseViewModel(application) {

    private val piggyRepository = PiggyRepository(
            AppDatabase.getInstance(application).piggyDataDao(),
            genericService().create(PiggybankService::class.java)
    )

    var accountId: Long = 0
        private set

    var accountName: String = ""
        private set

    fun getPiggyBankById(piggyBankId: Long): LiveData<PiggyData>{
        val piggyLiveData = MutableLiveData<PiggyData>()
        viewModelScope.launch(Dispatchers.IO){
            val piggyData = piggyRepository.getPiggyById(piggyBankId)
            accountId = piggyData.piggyAttributes?.account_id ?: 0
            accountName = piggyData.piggyAttributes?.account_name ?: ""
            piggyLiveData.postValue(piggyData)
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

}