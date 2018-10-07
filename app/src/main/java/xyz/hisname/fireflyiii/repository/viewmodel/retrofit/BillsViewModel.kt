package xyz.hisname.fireflyiii.repository.viewmodel.retrofit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.BillsService
import xyz.hisname.fireflyiii.repository.models.bills.BillApiResponse
import xyz.hisname.fireflyiii.util.retrofitCallback

class BillsViewModel: ViewModel() {


    fun getBill(baseUrl: String, accessToken: String): LiveData<BillApiResponse>{
        val apiResponse = MediatorLiveData<BillApiResponse>()
        val billResponse: MutableLiveData<BillApiResponse> = MutableLiveData()
        val billsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(BillsService::class.java)
        billsService?.getBills()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                billResponse.postValue(BillApiResponse(response.body()))
            }
        })
        { throwable ->  billResponse.postValue(BillApiResponse(throwable))})
        apiResponse.addSource(billResponse) {
            apiResponse.value = it
        }
        return apiResponse
    }

    fun deleteBill(baseUrl: String?, accessToken: String?, id: String): LiveData<BillApiResponse>{
        val apiResponse = MediatorLiveData<BillApiResponse>()
        val billResponse: MutableLiveData<BillApiResponse> = MutableLiveData()
        val billsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(BillsService::class.java)
        billsService?.deleteBillById(id)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                billResponse.postValue(BillApiResponse(response.body()))
            }
        })
        { throwable -> billResponse.postValue(BillApiResponse(throwable))})

        apiResponse.addSource(billResponse){ apiResponse.value = it }
        return apiResponse
    }

    fun addBill(baseUrl: String?, accessToken: String?, name: String, match: String,
                   amountMin: String, amountMax: String, date: String, repeatFreq: String,
                   skip: String,automatch: String,active: String,currencyId: String,notes: String?): LiveData<BillApiResponse>{
        val apiResponse = MediatorLiveData<BillApiResponse>()
        val billResponse: MutableLiveData<BillApiResponse> = MutableLiveData()
        val billsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(BillsService::class.java)
        billsService?.createBill(name, match, amountMin, amountMax, date,
                repeatFreq, skip, automatch, active, currencyId, notes)?.enqueue(retrofitCallback(
                { response ->
                    var errorBody = ""
                    if (response.errorBody() != null) {
                        errorBody = String(response.errorBody()?.bytes()!!)
                    }
                    if(response.isSuccessful){
                        billResponse.postValue(BillApiResponse(response.body()))
                    } else {
                        billResponse.postValue(BillApiResponse(errorBody))
                    }
                })
        { throwable -> billResponse.postValue(BillApiResponse(throwable)) })
        apiResponse.addSource(billResponse){ apiResponse.value = it }
        return apiResponse
    }
}