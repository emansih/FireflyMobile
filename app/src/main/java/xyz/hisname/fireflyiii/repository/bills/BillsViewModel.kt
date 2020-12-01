package xyz.hisname.fireflyiii.repository.bills

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.bills.BillSuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.workers.bill.DeleteBillWorker

class BillsViewModel(application: Application): BaseViewModel(application) {

    private val repository: BillRepository
    private var billData: MutableList<BillData> = arrayListOf()
    private val billsService by lazy { genericService()?.create(BillsService::class.java) }

    init {
        val billDataDao = AppDatabase.getInstance(application).billDataDao()
        repository = BillRepository(billDataDao, billsService)
    }

    fun getBillById(billId: Long): LiveData<MutableList<BillData>>{
        val billLiveData: MutableLiveData<MutableList<BillData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            billData = repository.retrieveBillById(billId)
        }.invokeOnCompletion {
            billLiveData.postValue(billData)
        }
        return billLiveData
    }

    fun deleteBillById(billId: Long): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        var isItDeleted = 0
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO){
            isItDeleted = repository.deleteBillById(billId)
        }.invokeOnCompletion {
            when (isItDeleted) {
                HttpConstants.FAILED -> {
                    isDeleted.postValue(false)
                    DeleteBillWorker.initPeriodicWorker(billId, getApplication())
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

    fun addBill(name: String, amountMin: String, amountMax: String, date: String, repeatFreq: String,
                skip: String, active: String,currencyId: String,notes: String?): LiveData<ApiResponses<BillSuccessModel>>{
        val apiResponse = MediatorLiveData<ApiResponses<BillSuccessModel>>()
        val apiLiveData: MutableLiveData<ApiResponses<BillSuccessModel>> = MutableLiveData()
        billsService?.createBill(name, amountMin, amountMax, date,
                repeatFreq, skip, active, currencyId, notes)?.enqueue(retrofitCallback(
                { response ->
                    var errorMessage = ""
                    val responseErrorBody = response.errorBody()
                    if (responseErrorBody != null) {
                        errorMessage = String(responseErrorBody.bytes())
                        val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorMessage)
                        errorMessage = when {
                            moshi?.errors?.name != null -> moshi.errors.name[0]
                            moshi?.errors?.currency_code != null -> moshi.errors.currency_code[0]
                            moshi?.errors?.amount_min != null -> moshi.errors.amount_min[0]
                            moshi?.errors?.repeat_freq != null -> moshi.errors.repeat_freq[0]
                            moshi?.errors?.date != null -> moshi.errors.date[0]
                            moshi?.errors?.skip != null -> moshi.errors.skip[0]
                            else -> "Error occurred while saving bill"
                        }
                    }
                    val networkData = response.body()
                    if (networkData != null) {
                        viewModelScope.launch(Dispatchers.IO) { repository.insertBill(networkData.data) }
                        apiLiveData.postValue(ApiResponses(response.body()))
                    } else {
                        apiLiveData.postValue(ApiResponses(errorMessage))
                    }
                })
        { throwable -> apiLiveData.postValue(ApiResponses(throwable)) })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }

    fun updateBill(billId: Long, name: String, amountMin: String, amountMax: String, date: String,
                   repeatFreq: String, skip: String,active: String,currencyId:
                   String,notes: String?): LiveData<ApiResponses<BillSuccessModel>>{
        val apiResponse = MediatorLiveData<ApiResponses<BillSuccessModel>>()
        val apiLiveData: MutableLiveData<ApiResponses<BillSuccessModel>> = MutableLiveData()
        billsService?.updateBill(billId, name, amountMin, amountMax, date,
                repeatFreq, skip, active, currencyId, notes)?.enqueue(retrofitCallback(
                { response ->
                    var errorMessage = ""
                    val responseErrorBody = response.errorBody()
                    if (responseErrorBody != null) {
                        errorMessage = String(responseErrorBody.bytes())
                        val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorMessage)
                        errorMessage = when {
                            moshi?.errors?.name != null -> moshi.errors.name[0]
                            moshi?.errors?.currency_code != null -> moshi.errors.currency_code[0]
                            moshi?.errors?.amount_min != null -> moshi.errors.amount_min[0]
                            moshi?.errors?.repeat_freq != null -> moshi.errors.repeat_freq[0]
                            moshi?.errors?.date != null -> moshi.errors.date[0]
                            moshi?.errors?.skip != null -> moshi.errors.skip[0]
                            else -> "Error occurred while saving bill"
                        }
                    }
                    val networkData = response.body()
                    if (networkData != null) {
                        viewModelScope.launch(Dispatchers.IO) { repository.insertBill(networkData.data) }
                        apiLiveData.postValue(ApiResponses(response.body()))
                    } else {
                        apiLiveData.postValue(ApiResponses(errorMessage))
                    }
                })
        { throwable -> apiLiveData.postValue(ApiResponses(throwable)) })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }

}