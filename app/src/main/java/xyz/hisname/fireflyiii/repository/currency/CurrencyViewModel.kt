package xyz.hisname.fireflyiii.repository.currency

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.models.currency.CurrencySuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class CurrencyViewModel(application: Application) : BaseViewModel(application) {

    val repository: CurrencyRepository

    private val currencyService by lazy { genericService()?.create(CurrencyService::class.java) }

    init {
        val currencyDataDao = AppDatabase.getInstance(application).currencyDataDao()
        repository = CurrencyRepository(currencyDataDao, currencyService)

    }

    fun getCurrencyById(currencyId: Long): LiveData<MutableList<CurrencyData>>{
        val currencyData: MutableLiveData<MutableList<CurrencyData>> = MutableLiveData()
        var data: MutableList<CurrencyData> = arrayListOf()
        viewModelScope.launch(Dispatchers.IO){
            data = repository.getCurrencyById(currencyId)
        }.invokeOnCompletion {
            currencyData.postValue(data)
        }
        return currencyData
    }

    fun updateCurrency(name: String, code: String, symbol: String, decimalPlaces: String,
                       enabled: Boolean, default: Boolean): LiveData<ApiResponses<CurrencySuccessModel>>{
        val apiResponse: MediatorLiveData<ApiResponses<CurrencySuccessModel>> =  MediatorLiveData()
        val apiLiveData: MutableLiveData<ApiResponses<CurrencySuccessModel>> = MutableLiveData()
        currencyService?.updateCurrency(code, name, code, symbol, decimalPlaces, enabled, default)?.enqueue(retrofitCallback({
            response ->
            var errorMessage = ""
            val responseErrorBody = response.errorBody()
            if (responseErrorBody != null){
                errorMessage = String(responseErrorBody.bytes())
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorMessage)
                errorMessage = when {
                    moshi?.errors?.name != null -> moshi.errors.name[0]
                    moshi?.errors?.code != null -> moshi.errors.code[0]
                    moshi?.errors?.symbol != null -> moshi.errors.symbol[0]
                    moshi?.errors?.decimalPlaces != null -> moshi.errors.decimalPlaces[0]
                    else -> "Error occurred while updating currency"
                }
            }
            val networkData = response.body()
            if (networkData != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.updateDefaultCurrency(networkData.data)
                }
                apiLiveData.postValue(ApiResponses(response.body()))
            } else {
                apiLiveData.postValue(ApiResponses(errorMessage))
            }

        })
        { throwable -> apiResponse.postValue(ApiResponses(throwable)) })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }

    fun addCurrency(name: String, code: String, symbol: String, decimalPlaces: String,
                    enabled: Boolean,default: Boolean): LiveData<ApiResponses<CurrencySuccessModel>>{
        val apiResponse: MediatorLiveData<ApiResponses<CurrencySuccessModel>> =  MediatorLiveData()
        val apiLiveData: MutableLiveData<ApiResponses<CurrencySuccessModel>> = MutableLiveData()
        currencyService?.createCurrency(name, code, symbol, decimalPlaces, enabled, default)?.enqueue(retrofitCallback({
            response ->
            var errorMessage = ""
            val responseErrorBody = response.errorBody()
            if (responseErrorBody != null){
                errorMessage = String(responseErrorBody.bytes())
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorMessage)
                errorMessage = when {
                    moshi?.errors?.name != null -> moshi.errors.name[0]
                    moshi?.errors?.code != null -> moshi.errors.code[0]
                    moshi?.errors?.symbol != null -> moshi.errors.symbol[0]
                    moshi?.errors?.decimalPlaces != null -> moshi.errors.decimalPlaces[0]
                    else -> "Error occurred while saving currency"
                }
            }
            val networkData = response.body()
            if (networkData != null) {
                viewModelScope.launch(Dispatchers.IO) { repository.insertCurrency(networkData.data) }
                apiLiveData.postValue(ApiResponses(response.body()))
            } else {
                apiLiveData.postValue(ApiResponses(errorMessage))
            }

        })
        { throwable -> apiResponse.postValue(ApiResponses(throwable)) })
        apiResponse.addSource(apiLiveData){ apiResponse.value = it }
        return apiResponse
    }

    fun getCurrencyByCode(currencyCode: String): LiveData<List<CurrencyData>>{
        var currencyData: List<CurrencyData> = listOf()
        val currencyLiveData: MutableLiveData<List<CurrencyData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            currencyData = repository.getCurrencyByCode(currencyCode)
        }.invokeOnCompletion {
            currencyLiveData.postValue(currencyData)
        }
        return currencyLiveData
    }

    fun getDefaultCurrency(): LiveData<MutableList<CurrencyData>>{
        val currencyLiveData: MutableLiveData<MutableList<CurrencyData>> = MutableLiveData()
        var currencyData: MutableList<CurrencyData> = arrayListOf()
        viewModelScope.launch(Dispatchers.IO){
            currencyData = repository.defaultCurrency()
        }.invokeOnCompletion {
            currencyLiveData.postValue(currencyData)
        }
        return currencyLiveData
    }
}