package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import okhttp3.ResponseBody
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.BillsService
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.bills.BillApiResponse
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.util.retrofitCallback

class BillsViewModel(application: Application) : AndroidViewModel(application) {

    private val billDatabase by lazy { AppDatabase.getInstance(application)?.billDataDao() }
    private var  billsService: BillsService? = null

    fun getBill(baseUrl: String, accessToken: String): BillResponse{
        val apiResponse = MediatorLiveData<BillApiResponse>()
        val billResponse: MutableLiveData<BillApiResponse> = MutableLiveData()
        billsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(BillsService::class.java)
        billsService?.getBills()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                response.body()?.data?.forEachIndexed { _, element ->
                    GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT, null, {
                        billDatabase?.addBill(element)
                    })
                }
            } else {
                var errorBody = ""
                if (response.errorBody() != null) {
                    errorBody = String(response.errorBody()?.bytes()!!)
                }
                billResponse.postValue(BillApiResponse(errorBody))
            }
        })
        { throwable ->  billResponse.postValue(BillApiResponse(throwable))})
        apiResponse.addSource(billResponse) {
            apiResponse.value = it
        }
        return BillResponse(billDatabase?.getAllBill(), apiResponse)
    }

    fun deleteBill(baseUrl: String?, accessToken: String?, id: String): LiveData<BillApiResponse>{
        val apiResponse = MediatorLiveData<BillApiResponse>()
        val billResponse: MutableLiveData<BillApiResponse> = MutableLiveData()
        val billsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(BillsService::class.java)
        billsService?.deleteBillById(id)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                billResponse.postValue(BillApiResponse(response.body()))
                GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT, null, {
                    billDatabase?.deleteBillById(id.toLong())
                })
            }
        })
        { throwable -> billResponse.postValue(BillApiResponse(throwable)) })
        apiResponse.addSource(billResponse){ apiResponse.value = it }
        return apiResponse
    }

    fun addBill(baseUrl: String?, accessToken: String?, name: String, match: String,
                   amountMin: String, amountMax: String, date: String, repeatFreq: String,
                   skip: String,automatch: String,active: String,currencyId: String,notes: String?): LiveData<BillApiResponse>{
        val apiResponse = MediatorLiveData<BillApiResponse>()
        val billResponse: MutableLiveData<BillApiResponse> = MutableLiveData()
        billsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(BillsService::class.java)
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

    fun updateBill(baseUrl: String?, accessToken: String?, billId: String, name: String, match: String,
                   amountMin: String, amountMax: String, date: String, repeatFreq: String,
                   skip: String,automatch: String,active: String,currencyId: String,notes: String?): LiveData<BillApiResponse>{
        val apiResponse = MediatorLiveData<BillApiResponse>()
        val billResponse: MutableLiveData<BillApiResponse> = MutableLiveData()
        billsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(BillsService::class.java)
        billsService?.updateBill(billId, name, match, amountMin, amountMax, date,
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

    fun getBillById(id: Long, baseUrl: String, accessToken: String): BillResponse{
        val apiResponse = MediatorLiveData<BillApiResponse>()
        val billResponse: MutableLiveData<BillApiResponse> = MutableLiveData()
        billsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(BillsService::class.java)
        billsService?.getBillById(id.toString())?.enqueue(retrofitCallback({ response ->
            if (!response.isSuccessful) {
                var errorBody = ""
                if (response.errorBody() != null) {
                    errorBody = String(response.errorBody()?.bytes()!!)
                }
                billResponse.postValue(BillApiResponse(errorBody))
            }
        })
        { throwable ->  billResponse.postValue(BillApiResponse(throwable))})
        apiResponse.addSource(billResponse) {
            apiResponse.value = it
        }
        return BillResponse(billDatabase?.getBillById(id), apiResponse)
    }
}

data class BillResponse(val databaseData: LiveData<MutableList<BillData>>?, val apiResponse: MediatorLiveData<BillApiResponse>)