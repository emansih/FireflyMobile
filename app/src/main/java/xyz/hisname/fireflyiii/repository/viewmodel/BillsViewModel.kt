package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.BillsService
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.BaseResponse
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.bills.BillsModel
import xyz.hisname.fireflyiii.repository.models.bills.success.BillSucessModel
import xyz.hisname.fireflyiii.util.retrofitCallback

class BillsViewModel(application: Application) : AndroidViewModel(application) {

    private val billDatabase by lazy { AppDatabase.getInstance(application)?.billDataDao() }
    private var billsService: BillsService? = null
    private val billResponse: MutableLiveData<ApiResponses<BillsModel>> = MutableLiveData()


    fun getBill(baseUrl: String, accessToken: String): BaseResponse<BillData, ApiResponses<BillsModel>> {
        val apiResponse = MediatorLiveData<ApiResponses<BillsModel>>()
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
                billResponse.postValue(ApiResponses(errorBody))
            }
        })
        { throwable ->  billResponse.postValue(ApiResponses(throwable))})
        apiResponse.addSource(billResponse) {
            apiResponse.value = it
        }
        return BaseResponse(billDatabase?.getAllBill(), apiResponse)
    }

    fun deleteBill(baseUrl: String?, accessToken: String?, id: String): LiveData<ApiResponses<BillsModel>>{
        val apiResponse = MediatorLiveData<ApiResponses<BillsModel>>()
        val billsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(BillsService::class.java)
        billsService?.deleteBillById(id)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                billResponse.postValue(ApiResponses(response.body()))
                GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT, null, {
                    billDatabase?.deleteBillById(id.toLong())
                })
            }
        })
        { throwable -> billResponse.postValue(ApiResponses(throwable)) })
        apiResponse.addSource(billResponse){ apiResponse.value = it }
        return apiResponse
    }

    fun addBill(baseUrl: String?, accessToken: String?, name: String, match: String,
                   amountMin: String, amountMax: String, date: String, repeatFreq: String,
                   skip: String,automatch: String,active: String,currencyId: String,notes: String?): LiveData<ApiResponses<BillSucessModel>>{
        val apiResponse = MediatorLiveData<ApiResponses<BillSucessModel>>()
        val apiLiveData: MutableLiveData<ApiResponses<BillSucessModel>> = MutableLiveData()
        billsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(BillsService::class.java)
        billsService?.createBill(name, match, amountMin, amountMax, date,
                repeatFreq, skip, automatch, active, currencyId, notes)?.enqueue(retrofitCallback(
                { response ->
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
        { throwable -> apiLiveData.postValue(ApiResponses(throwable)) })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }

    fun updateBill(baseUrl: String?, accessToken: String?, billId: String, name: String, match: String,
                   amountMin: String, amountMax: String, date: String, repeatFreq: String,
                   skip: String,automatch: String,active: String,currencyId: String,notes: String?): LiveData<ApiResponses<BillSucessModel>>{
        val apiResponse = MediatorLiveData<ApiResponses<BillSucessModel>>()
        val apiLiveData: MutableLiveData<ApiResponses<BillSucessModel>> = MutableLiveData()
        billsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(BillsService::class.java)
        billsService?.updateBill(billId, name, match, amountMin, amountMax, date,
                repeatFreq, skip, automatch, active, currencyId, notes)?.enqueue(retrofitCallback(
                { response ->
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
        { throwable -> apiLiveData.postValue(ApiResponses(throwable)) })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }

    fun getBillById(id: Long, baseUrl: String, accessToken: String): BaseResponse<BillData, ApiResponses<BillsModel>>{
        val apiResponse = MediatorLiveData<ApiResponses<BillsModel>>()
        billsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(BillsService::class.java)
        billsService?.getBillById(id.toString())?.enqueue(retrofitCallback({ response ->
            if (!response.isSuccessful) {
                var errorBody = ""
                if (response.errorBody() != null) {
                    errorBody = String(response.errorBody()?.bytes()!!)
                }
                billResponse.postValue(ApiResponses(errorBody))
            }
        })
        { throwable ->  billResponse.postValue(ApiResponses(throwable))})
        apiResponse.addSource(billResponse) {
            apiResponse.value = it
        }
        return BaseResponse(billDatabase?.getBillById(id), apiResponse)
    }
}
