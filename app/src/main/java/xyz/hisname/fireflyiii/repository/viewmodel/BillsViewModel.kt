package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.work.*
import com.google.gson.Gson
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
import xyz.hisname.fireflyiii.repository.models.bills.BillSuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.workers.bill.DeleteBillWorker
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
                    GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
                        billDatabase?.addBill(element)
                    }
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
                GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
                    billDatabase?.deleteBillById(id.toLong())
                }
            } else {
                val billTag = WorkManager.getInstance().getStatusesByTag("delete_bill_$id").get()
                if(billTag == null || billTag.size == 0){
                    val billData = Data.Builder()
                            .putString("id", id)
                            .build()
                    val deleteBillWork = OneTimeWorkRequest.Builder(DeleteBillWorker::class.java)
                            .setInputData(billData)
                            .addTag("delete_bill_$id")
                            .setConstraints(Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED).build())
                            .build()
                    WorkManager.getInstance().enqueue(deleteBillWork)
                    billResponse.postValue(ApiResponses("There is an error deleting your bill, we will do it again later."))
                } else {
                    billResponse.postValue(ApiResponses("This bill has already been queued to delete."))
                }
            }
        })
        { throwable -> billResponse.postValue(ApiResponses(throwable)) })
        apiResponse.addSource(billResponse){ apiResponse.value = it }
        return apiResponse
    }

    fun addBill(baseUrl: String?, accessToken: String?, name: String, match: String,
                   amountMin: String, amountMax: String, date: String, repeatFreq: String,
                   skip: String,automatch: String,active: String,currencyId: String,notes: String?): LiveData<ApiResponses<BillSuccessModel>>{
        val apiResponse = MediatorLiveData<ApiResponses<BillSuccessModel>>()
        val apiLiveData: MutableLiveData<ApiResponses<BillSuccessModel>> = MutableLiveData()
        billsService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(BillsService::class.java)
        billsService?.createBill(name, match, amountMin, amountMax, date,
                repeatFreq, skip, automatch, active, currencyId, notes)?.enqueue(retrofitCallback(
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
                            gson.errors.automatch != null -> gson.errors.automatch[0]
                            else -> "Error occurred while saving bill"
                        }
                    }

                    if(response.isSuccessful){
                        apiLiveData.postValue(ApiResponses(response.body()))
                    } else {
                        apiLiveData.postValue(ApiResponses(errorMessage))
                    }
                })
        { throwable -> apiLiveData.postValue(ApiResponses(throwable)) })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }

    fun updateBill(baseUrl: String?, accessToken: String?, billId: String, name: String, match: String,
                   amountMin: String, amountMax: String, date: String, repeatFreq: String,
                   skip: String,automatch: String,active: String,currencyId: String,notes: String?): LiveData<ApiResponses<BillSuccessModel>>{
        val apiResponse = MediatorLiveData<ApiResponses<BillSuccessModel>>()
        val apiLiveData: MutableLiveData<ApiResponses<BillSuccessModel>> = MutableLiveData()
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

    fun getBillById(id: Long) = billDatabase?.getBillById(id)
}
