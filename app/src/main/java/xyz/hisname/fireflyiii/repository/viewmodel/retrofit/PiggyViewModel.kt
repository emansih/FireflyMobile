package xyz.hisname.fireflyiii.repository.viewmodel.retrofit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.PiggybankService
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyApiResponse
import xyz.hisname.fireflyiii.util.retrofitCallback

class PiggyViewModel: ViewModel() {

    fun getPiggyBanks(baseUrl: String?, accessToken: String?): LiveData<PiggyApiResponse> {
        val apiResponse: MediatorLiveData<PiggyApiResponse> =  MediatorLiveData()
        val piggy: MutableLiveData<PiggyApiResponse> = MutableLiveData()
        val piggyBankService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(PiggybankService::class.java)
        piggyBankService?.getPiggyBanks()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                piggy.value = PiggyApiResponse(response.body())
            }
        })
        { throwable ->  piggy.value = PiggyApiResponse(throwable)})

        apiResponse.addSource(piggy) { apiResponse.value = it }
        return apiResponse
    }

    fun deletePiggyBank(baseUrl: String?, accessToken: String?, id: String): LiveData<PiggyApiResponse>{
        val apiResponse: MediatorLiveData<PiggyApiResponse> =  MediatorLiveData()
        val piggy: MutableLiveData<PiggyApiResponse> = MutableLiveData()
        val piggyBankService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(PiggybankService::class.java)
        piggyBankService?.deletePiggyBankById(id)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                piggy.value = PiggyApiResponse(response.body())
            }
        })
        { throwable -> piggy.value = PiggyApiResponse(throwable)})
        apiResponse.addSource(piggy){ apiResponse.value = it }
        return apiResponse
    }

    fun addPiggyBank(baseUrl: String?, accessToken: String?, piggyName: String, accountId: String,
                     currentAmount: String, notes: String, startDate: String, targetAmount: String,
                     targetDate: String): LiveData<PiggyApiResponse>{
        val apiResponse: MediatorLiveData<PiggyApiResponse> =  MediatorLiveData()
        val piggy: MutableLiveData<PiggyApiResponse> = MutableLiveData()
        val piggyBankService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(PiggybankService::class.java)
        piggyBankService?.createNewPiggyBank(piggyName, accountId, targetAmount, currentAmount, startDate,
                targetDate, notes)?.enqueue(retrofitCallback({ response ->
            piggy.postValue(PiggyApiResponse(String(response.errorBody()?.bytes()!!)))
        })
        { throwable -> apiResponse.postValue(PiggyApiResponse(throwable)) })
        apiResponse.addSource(piggy){ apiResponse.value = it }
        return apiResponse

    }
}