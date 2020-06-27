package xyz.hisname.fireflyiii.repository.currency

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.Response
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyModel
import xyz.hisname.fireflyiii.repository.models.currency.CurrencySuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.Version
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

    fun getCurrency(pageNumber: Int): LiveData<MutableList<CurrencyData>>{
        var currencyData: MutableList<CurrencyData> = arrayListOf()
        isLoading.value = true
        val data: MutableLiveData<MutableList<CurrencyData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            currencyData = repository.getPaginatedCurrency(pageNumber)
        }.invokeOnCompletion {
            data.postValue(currencyData)
            isLoading.postValue(false)
        }
        return data
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
                val gson = Gson().fromJson(errorMessage, ErrorModel::class.java)
                errorMessage = when {
                    gson.errors.name != null -> gson.errors.name[0]
                    gson.errors.code != null -> gson.errors.code[0]
                    gson.errors.symbol != null -> gson.errors.symbol[0]
                    gson.errors.decimalPlaces != null -> gson.errors.decimalPlaces[0]
                    else -> "Error occurred while updating currency"
                }
            }
            val networkData = response.body()
            if (networkData != null) {
                var defaultCurrency: MutableList<CurrencyData> = arrayListOf()
                viewModelScope.launch(Dispatchers.IO) {
                    defaultCurrency = repository.defaultCurrencyWithoutNetwork()
                }.invokeOnCompletion {
                    // This is needed otherwise we might get wrong default currency
                    if(defaultCurrency[0].currencyAttributes?.name == name &&
                            defaultCurrency[0].currencyAttributes?.currencyDefault != default){
                        viewModelScope.launch(Dispatchers.IO) { repository.deleteDefaultCurrency() }
                    }
                    viewModelScope.launch(Dispatchers.IO) { repository.insertCurrency(networkData.data) }
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

    fun getCurrencyByCode(currencyCode: String): LiveData<MutableList<CurrencyData>>{
        val currencyLiveData: MutableLiveData<MutableList<CurrencyData>> = MutableLiveData()
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
        val fireflyVersionNumber = AppPref(sharedPref).serverVersion
        val versionNumbering = Version(fireflyVersionNumber).compareTo(Version("5.3.0"))
        viewModelScope.launch(Dispatchers.IO){
            if(versionNumbering == -1){
                repository.loadAllData()
            } else {
                repository.defaultCurrencyWithNetwork()
            }
            currencyData = repository.defaultCurrencyWithoutNetwork()
        }.invokeOnCompletion {
            currencyLiveData.postValue(currencyData)
        }
        return currencyLiveData
    }

    fun setCurrencyCode(code: String?) {
        currencyCode.value = code
    }

    fun setFullDetails(details: String?){
        currencyDetails.value = details
    }
}