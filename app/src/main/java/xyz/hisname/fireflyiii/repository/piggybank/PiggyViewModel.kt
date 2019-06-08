package xyz.hisname.fireflyiii.repository.piggybank

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.api.PiggybankService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggySuccessModel
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class PiggyViewModel(application: Application): BaseViewModel(application)  {

    val repository: PiggyRepository
    private val piggyService by lazy { genericService()?.create(PiggybankService::class.java) }
    val piggyName =  MutableLiveData<String>()

    init {
        val piggyDataDao = AppDatabase.getInstance(application).piggyDataDao()
        repository = PiggyRepository(piggyDataDao, piggyService)
    }

    fun getAllPiggyBanks(): LiveData<MutableList<PiggyData>> {
        lateinit var piggyData: MutableList<PiggyData>
        val data: MutableLiveData<MutableList<PiggyData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            piggyData = repository.allPiggyBanks()
        }.invokeOnCompletion {
            data.postValue(piggyData)
        }
        return data
    }

    fun getPiggyById(piggyId: Long): LiveData<MutableList<PiggyData>>{
        val piggyData: MutableLiveData<MutableList<PiggyData>> = MutableLiveData()
        lateinit var data: MutableList<PiggyData>
        viewModelScope.launch(Dispatchers.IO){
            data = repository.retrievePiggyById(piggyId)
        }.invokeOnCompletion {
            piggyData.postValue(data)
        }
        return piggyData
    }

    fun deletePiggyById(piggyId: Long): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        isLoading.value = true
        var isItDeleted = false
        viewModelScope.launch(Dispatchers.IO) {
            isItDeleted = repository.deletePiggyById(piggyId, true)
        }.invokeOnCompletion {
            if(isItDeleted) {
                isDeleted.postValue(true)
            } else {
                isDeleted.postValue(false)
            }
            isLoading.postValue(false)

        }
        return isDeleted
    }

    fun addPiggyBank(piggyName: String, accountId: String,
                     currentAmount: String?, notes: String?, startDate: String?, targetAmount: String,
                     targetDate: String?): LiveData<ApiResponses<PiggySuccessModel>>{
        val apiResponse: MediatorLiveData<ApiResponses<PiggySuccessModel>> =  MediatorLiveData()
        val apiLiveData: MutableLiveData<ApiResponses<PiggySuccessModel>> = MutableLiveData()
        piggyService?.createNewPiggyBank(piggyName, accountId, targetAmount, currentAmount, startDate,
                targetDate, notes)?.enqueue(retrofitCallback({ response ->
            var errorMessage = ""
            val responseErrorBody = response.errorBody()
            if (responseErrorBody != null) {
                errorMessage = String(responseErrorBody.bytes())
                val gson = Gson().fromJson(errorMessage, ErrorModel::class.java)
                errorMessage = when {
                    gson.errors.name != null -> gson.errors.name[0]
                    gson.errors.account_id != null -> gson.errors.account_id[0]
                    gson.errors.current_amount != null -> gson.errors.current_amount[0]
                    gson.errors.targetDate != null -> gson.errors.targetDate[0]
                    else -> "Error occurred while saving piggy bank"
                }
            }
            val networkData = response.body()
            if (networkData != null) {
                viewModelScope.launch(Dispatchers.IO) { repository.insertPiggy(networkData.data) }
                apiLiveData.postValue(ApiResponses(response.body()))
            } else {
                apiLiveData.postValue(ApiResponses(errorMessage))
            }
        })
        { throwable ->
            apiResponse.postValue(ApiResponses(throwable))
        })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }

    fun updatePiggyBank(piggyId: Long, piggyName: String, accountId: String,
                        currentAmount: String?, notes: String?, startDate: String?, targetAmount: String,
                        targetDate: String?): LiveData<ApiResponses<PiggySuccessModel>> {
        val apiResponse: MediatorLiveData<ApiResponses<PiggySuccessModel>> = MediatorLiveData()
        val apiLiveData: MutableLiveData<ApiResponses<PiggySuccessModel>> = MutableLiveData()
        piggyService?.updatePiggyBank(piggyId, piggyName, accountId, targetAmount, currentAmount, startDate,
                targetDate, notes)?.enqueue(retrofitCallback({ response ->
            var errorMessage = ""
            val responseErrorBody = response.errorBody()
            if (responseErrorBody != null) {
                errorMessage = String(responseErrorBody.bytes())
                val gson = Gson().fromJson(errorMessage, ErrorModel::class.java)
                errorMessage = when {
                    gson.errors.name != null -> gson.errors.name[0]
                    gson.errors.account_id != null -> gson.errors.account_id[0]
                    gson.errors.current_amount != null -> gson.errors.current_amount[0]
                    gson.errors.targetDate != null -> gson.errors.targetDate[0]
                    else -> "Error occurred while updating piggy bank"
                }
            }
            val networkData = response.body()
            if (networkData != null) {
                viewModelScope.launch(Dispatchers.IO) { repository.insertPiggy(networkData.data) }
                apiLiveData.postValue(ApiResponses(response.body()))
            } else {
                apiLiveData.postValue(ApiResponses(errorMessage))
            }
        })
        { throwable ->
            apiResponse.postValue(ApiResponses(throwable))
        })
        apiResponse.addSource(apiLiveData) { apiResponse.value = it }
        return apiResponse
    }

    fun getPiggyByName(piggyBankName: String): LiveData<MutableList<PiggyData>>{
        var piggyData: MutableList<PiggyData> = arrayListOf()
        val data: MutableLiveData<MutableList<PiggyData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            piggyData = repository.searchPiggyByName("%$piggyBankName%")
        }.invokeOnCompletion {
            data.postValue(piggyData)
        }
        return data
    }

    fun postPiggyName(details: String?){
        piggyName.value = details
    }

    fun getNonCompletedPiggyBanks(): LiveData<MutableList<PiggyData>>{
        var piggyData: MutableList<PiggyData> = arrayListOf()
        val data: MutableLiveData<MutableList<PiggyData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            piggyData = repository.getNonCompletedPiggyBanks()
        }.invokeOnCompletion {
            data.postValue(piggyData)
        }
        return data
    }

}