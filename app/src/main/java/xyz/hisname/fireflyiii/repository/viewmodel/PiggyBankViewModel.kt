package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.PiggybankService
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.BaseResponse
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyModel
import xyz.hisname.fireflyiii.repository.models.piggy.success.PiggySuccessModel
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
                    GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT, null, {
                        piggyDataBase?.addPiggy(element)
                    })
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
                GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT, null, {
                    piggyDataBase?.deletePiggyById(id.toLong())
                })
            } else {
                var errorBody = ""
                if (response.errorBody() != null) {
                    errorBody = String(response.errorBody()?.bytes()!!)
                }
                apiLiveData.postValue(ApiResponses(errorBody))
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
            var errorBody = ""
            if (response.errorBody() != null) {
                errorBody = String(response.errorBody()?.bytes()!!)
            }
            if(response.isSuccessful){
                apiLiveData.postValue(ApiResponses(response.body()))
            } else {
                apiLiveData.postValue(ApiResponses(errorBody))
            }
        })
        { throwable ->
            apiResponse.postValue(ApiResponses(throwable))
        })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse

    }

    fun getPiggyBankById(id: Long, baseUrl: String, accessToken: String): BaseResponse<PiggyData, ApiResponses<PiggyModel>>{
        val apiResponse = MediatorLiveData<ApiResponses<PiggyModel>>()
        piggyBankService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(PiggybankService::class.java)
        piggyBankService?.getPiggyBankById(id.toString())?.enqueue(retrofitCallback({ response ->
            if (!response.isSuccessful) {
                var errorBody = ""
                if (response.errorBody() != null) {
                    errorBody = String(response.errorBody()?.bytes()!!)
                }
                apiLiveData.postValue(ApiResponses(errorBody))
            }
        })
        { throwable ->  apiLiveData.postValue(ApiResponses(throwable))})
        apiResponse.addSource(apiLiveData) {
            apiResponse.value = it
        }
        return BaseResponse(piggyDataBase?.getPiggyById(id), apiResponse)
    }
}