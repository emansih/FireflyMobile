package xyz.hisname.fireflyiii.repository.currency

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.data.remote.api.CurrencyService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.models.currency.CurrencySuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class CurrencyViewModel(application: Application) : BaseViewModel(application) {

    val currencyCode =  MutableLiveData<String>()
    val currencyDetails = MutableLiveData<String>()
    val repository: CurrencyRepository
    private var currencyData: MutableList<CurrencyData> = arrayListOf()

    private val currencyService by lazy { genericService()?.create(CurrencyService::class.java) }

    init {
        val currencyDataDao = AppDatabase.getInstance(application).currencyDataDao()
        repository = CurrencyRepository(currencyDataDao)

    }

    fun getCurrency(): LiveData<MutableList<CurrencyData>> {
        loadRemoteData()
        return repository.allCurrency
    }

    fun addCurrency(name: String, code: String, symbol: String, decimalPlaces: String,
                    enabled: Boolean): LiveData<ApiResponses<CurrencySuccessModel>>{
        val apiResponse: MediatorLiveData<ApiResponses<CurrencySuccessModel>> =  MediatorLiveData()
        val apiLiveData: MutableLiveData<ApiResponses<CurrencySuccessModel>> = MutableLiveData()
        currencyService?.createCurrency(name, code, symbol, decimalPlaces, enabled)?.enqueue(retrofitCallback({
            response ->
            var errorMessage = ""
            val responseErrorBody = response.errorBody()
            if (responseErrorBody != null){
                errorMessage = String(responseErrorBody.bytes())
                val gson = Gson().fromJson(errorMessage, ErrorModel::class.java)
                errorMessage = when {
                    gson.errors.name != null -> gson.errors.name[0]
                    gson.errors.code != null -> gson.errors.code[0]
                    gson.errors.symbol != null -> gson.errors.symbol[0]
                    gson.errors.decimalPlaces != null -> gson.errors.decimalPlaces[0]
                    else -> "Error occurred while saving currency"
                }
            }
            val networkData = response.body()
            if (networkData != null) {
                scope.launch(Dispatchers.IO) { repository.insertCurrency(networkData.data) }
                apiLiveData.postValue(ApiResponses(response.body()))
            } else {
                apiLiveData.postValue(ApiResponses(errorMessage))
            }

        })
        { throwable -> apiResponse.postValue(ApiResponses(throwable)) })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }

    fun getCurrencyByCode(currencyCode: String): LiveData<MutableList<CurrencyData>>{
        val currencyLiveData: MutableLiveData<MutableList<CurrencyData>> = MutableLiveData()
        scope.async(Dispatchers.IO){
            currencyData = repository.getCurrencyByCode(currencyCode)
        }.invokeOnCompletion {
            currencyLiveData.postValue(currencyData)
        }
        return currencyLiveData
    }

    fun getDefaultCurrency(): LiveData<MutableList<CurrencyData>>{
        loadRemoteData()
        return repository.defaultCurrency
    }


    fun setCurrencyCode(code: String?) {
        currencyCode.value = code
    }

    fun setFullDetails(details: String?){
        currencyDetails.value = details
    }

    private fun loadRemoteData(){
        isLoading.value = true
        currencyService?.getPaginatedCurrency(1)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val networkData = response.body()
                if (networkData != null) {
                    if(networkData.meta.pagination.current_page == networkData.meta.pagination.total_pages){
                        networkData.data.forEachIndexed { _, currencyData ->
                            scope.launch(Dispatchers.IO) { repository.insertCurrency(currencyData) }
                        }
                    } else {
                        networkData.data.forEachIndexed { _, currencyData ->
                            scope.launch(Dispatchers.IO) { repository.insertCurrency(currencyData) }
                        }
                        for (pagination in 2..networkData.meta.pagination.total_pages) {
                            currencyService?.getPaginatedCurrency(pagination)?.enqueue(retrofitCallback({ respond ->
                                respond.body()?.data?.forEachIndexed { _, currencyPagination ->
                                    scope.launch(Dispatchers.IO) { repository.insertCurrency(currencyPagination) }
                                }
                            }))
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
            }
        })
        { throwable -> apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage)) })
        isLoading.value = false
    }
}