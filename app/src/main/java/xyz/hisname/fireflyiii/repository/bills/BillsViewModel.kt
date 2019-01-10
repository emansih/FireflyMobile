package xyz.hisname.fireflyiii.repository.bills

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.api.BillsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.bills.BillSuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import xyz.hisname.fireflyiii.workers.bill.DeleteBillWorker

class BillsViewModel(application: Application): BaseViewModel(application) {

    val repository: BillRepository
    private var billData: MutableList<BillData> = arrayListOf()
    private val billsService by lazy { genericService()?.create(BillsService::class.java) }

    init {
        val billDataDao = AppDatabase.getInstance(application).billDataDao()
        repository = BillRepository(billDataDao)
    }


    fun getAllBills(): LiveData<MutableList<BillData>>{
        isLoading.value = true
        var billData: MutableList<BillData> = arrayListOf()
        val data: MutableLiveData<MutableList<BillData>> = MutableLiveData()
        billsService?.getPaginatedBills(1)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val responseBody = response.body()
                if(responseBody != null){
                    billData.addAll(responseBody.data)
                    if(responseBody.meta.pagination.total_pages > responseBody.meta.pagination.current_page){
                        for(items in 2..responseBody.meta.pagination.total_pages){
                            billsService?.getPaginatedBills(items)?.enqueue(retrofitCallback({ pagination ->
                                pagination.body()?.data?.forEachIndexed{ _, dataResponse ->
                                    billData.add(dataResponse)
                                }

                            }))
                        }
                    }
                }
                data.postValue(billData.toMutableList())
                scope.launch(Dispatchers.IO){
                    repository.deleteAllBills()
                }.invokeOnCompletion {
                    billData.forEachIndexed { _, billData ->
                        scope.launch(Dispatchers.IO){
                            repository.insertBill(billData)
                        }
                    }
                }
            } else {
                val responseError = response.errorBody()
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    apiResponse.postValue(gson.message)
                }
                scope.async(Dispatchers.IO) {
                    billData = repository.allBills()
                }.invokeOnCompletion {
                    data.postValue(billData)
                }
            }
            isLoading.value = false
        })
        { throwable ->
            scope.async(Dispatchers.IO) {
                billData = repository.allBills()
            }.invokeOnCompletion {
                data.postValue(billData)
            }
            isLoading.value = false
            apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage))
        })
        return data
    }

    fun getBillById(billId: Long): LiveData<MutableList<BillData>>{
        val billLiveData: MutableLiveData<MutableList<BillData>> = MutableLiveData()
        scope.async(Dispatchers.IO){
            billData = repository.retrieveBillById(billId)
        }.invokeOnCompletion {
            billLiveData.postValue(billData)
        }
        return billLiveData
    }

    fun deleteBillById(billId: Long): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        isLoading.value = true
        billsService?.deleteBillById(billId)?.enqueue(retrofitCallback({ response ->
            if (response.code() == 204 || response.code() == 200) {
                scope.async(Dispatchers.IO) {
                    repository.deleteBillById(billId)
                }.invokeOnCompletion {
                    isDeleted.postValue(true)
                }
            } else {
                isDeleted.postValue(false)
                deleteBill(billId)
            }
        })
        { throwable ->
            isDeleted.postValue(false)
            deleteBill(billId)
        })
        isLoading.value = false
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
                        val gson = Gson().fromJson(errorMessage, ErrorModel::class.java)
                        errorMessage = when {
                            gson.errors.name != null -> gson.errors.name[0]
                            gson.errors.currency_code != null -> gson.errors.currency_code[0]
                            gson.errors.amount_min != null -> gson.errors.amount_min[0]
                            gson.errors.repeat_freq != null -> gson.errors.repeat_freq[0]
                            gson.errors.date != null -> gson.errors.date[0]
                            gson.errors.skip != null -> gson.errors.skip[0]
                            else -> "Error occurred while saving bill"
                        }
                    }
                    val networkData = response.body()
                    if (networkData != null) {
                        scope.launch(Dispatchers.IO) { repository.insertBill(networkData.data) }
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
                    var errorBody = ""
                    if (response.errorBody() != null) {
                        errorBody = String(response.errorBody()?.bytes()!!)
                    }
                    val networkData = response.body()
                    if (networkData != null) {
                        scope.launch(Dispatchers.IO) { repository.updateBill(networkData.data) }
                        apiLiveData.postValue(ApiResponses(response.body()))
                    } else {
                        apiLiveData.postValue(ApiResponses(errorBody))
                    }
                })
        { throwable -> apiLiveData.postValue(ApiResponses(throwable)) })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }

    private fun deleteBill(billId: Long){
        val accountTag =
                WorkManager.getInstance().getWorkInfosByTag("delete_bill_$billId").get()
        if(accountTag == null || accountTag.size == 0) {
            val accountData = Data.Builder()
                    .putLong("billId", billId)
                    .build()
            val deleteAccountWork = OneTimeWorkRequest.Builder(DeleteBillWorker::class.java)
                    .setInputData(accountData)
                    .addTag("delete_bill_$billId")
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build()
            WorkManager.getInstance().enqueue(deleteAccountWork)
        }
    }
}