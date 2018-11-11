package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.PiggybankService
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.BaseResponse
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyModel
import xyz.hisname.fireflyiii.repository.models.piggy.PiggySuccessModel
import xyz.hisname.fireflyiii.repository.workers.piggybank.DeletePiggyWorker
import xyz.hisname.fireflyiii.util.retrofitCallback

class PiggyBankViewModel(application: Application) : AndroidViewModel(application) {

    private val piggyDataBase by lazy { AppDatabase.getInstance(application)?.piggyDataDao() }
    private var piggyBankService: PiggybankService? = null
    private val apiLiveData: MutableLiveData<ApiResponses<PiggyModel>> = MutableLiveData()


    fun getPiggyBank(baseUrl: String?, accessToken: String?): BaseResponse<PiggyData, ApiResponses<PiggyModel>>{
        val apiResponse: MediatorLiveData<ApiResponses<PiggyModel>> =  MediatorLiveData()
        piggyBankService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(PiggybankService::class.java)
        piggyBankService?.getPiggyBanks()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                response.body()?.data?.forEachIndexed { _, element ->
                    GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
                        piggyDataBase?.insert(element)
                    }
                }
            } else {
                var errorBody = ""
                if (response.errorBody() != null) {
                    errorBody = String(response.errorBody()?.bytes()!!)
                }
                apiLiveData.postValue(ApiResponses(errorBody))
            }
        })
        { throwable ->  apiLiveData.value = ApiResponses(throwable)})

        apiResponse.addSource(apiLiveData) { apiResponse.value = it }
        return BaseResponse(piggyDataBase?.getPiggy(), apiResponse)
    }

    fun deletePiggyBank(baseUrl: String?, accessToken: String?,id: String): LiveData<ApiResponses<PiggyModel>>{
        val apiResponse: MediatorLiveData<ApiResponses<PiggyModel>> =  MediatorLiveData()
        piggyBankService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(PiggybankService::class.java)
        piggyBankService?.deletePiggyBankById(id)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                apiResponse.postValue(ApiResponses(response.body()))
                GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
                    piggyDataBase?.deletePiggyById(id.toLong())
                }
            } else {
                val piggyTag =
                        WorkManager.getInstance().getStatusesByTag("delete_piggy_bank_$id").get()
                if(piggyTag == null || piggyTag.size == 0) {
                    apiLiveData.postValue(ApiResponses("There is an error deleting your piggy bank, we will do it again later."))
                    val piggyData = Data.Builder()
                            .putString("id", id)
                            .build()
                    val deletePiggyWork = OneTimeWorkRequest.Builder(DeletePiggyWorker::class.java)
                            .setInputData(piggyData)
                            .addTag("delete_piggy_bank_$id")
                            .setConstraints(Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED).build())
                            .build()
                    WorkManager.getInstance().enqueue(deletePiggyWork)
                } else {
                    apiLiveData.postValue(ApiResponses("This Piggy Bank has already been queued to delete."))
                }
            }
        })
        { throwable -> apiLiveData.value = ApiResponses(throwable)})
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }

    fun addPiggyBank(baseUrl: String?, accessToken: String?, piggyName: String, accountId: String,
                     currentAmount: String?, notes: String?, startDate: String?, targetAmount: String,
                     targetDate: String?): LiveData<ApiResponses<PiggySuccessModel>>{
        val apiResponse: MediatorLiveData<ApiResponses<PiggySuccessModel>> =  MediatorLiveData()
        val apiLiveData: MutableLiveData<ApiResponses<PiggySuccessModel>> = MutableLiveData()
        piggyBankService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(PiggybankService::class.java)
        piggyBankService?.createNewPiggyBank(piggyName, accountId, targetAmount, currentAmount, startDate,
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
            if(response.isSuccessful){
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

    fun getPiggyBankById(id: Long)=  piggyDataBase?.getPiggyById(id)


}