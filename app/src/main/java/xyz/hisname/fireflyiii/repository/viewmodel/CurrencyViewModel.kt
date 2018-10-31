package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.CurrencyService
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.BaseResponse
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyModel
import xyz.hisname.fireflyiii.util.retrofitCallback

class CurrencyViewModel(application: Application) : AndroidViewModel(application) {

    private val currencyDatabase by lazy { AppDatabase.getInstance(application)?.currencyDataDao() }
    private var currencyService: CurrencyService? = null
    private val apiLiveData: MutableLiveData<ApiResponses<CurrencyModel>> = MutableLiveData()


    fun getCurrency(baseUrl: String, accessToken: String): BaseResponse<CurrencyData, ApiResponses<CurrencyModel>>{
        val apiResponse = MediatorLiveData<ApiResponses<CurrencyModel>>()
        currencyService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(CurrencyService::class.java)
        currencyService?.getCurrency()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                response.body()?.data?.forEachIndexed { _, element ->
                    GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT, null, {
                        currencyDatabase?.addCurrency(element)
                    })
                }
            } else {
                var errorBody = ""
                if (response.errorBody() != null) {
                    errorBody = String(response.errorBody()?.bytes()!!)
                }
                apiLiveData.postValue(ApiResponses(errorBody))
            }
        })
        { throwable ->  apiResponse.postValue(ApiResponses(throwable))})
        apiResponse.addSource(apiLiveData) {
            apiResponse.value = it
        }
        return BaseResponse(currencyDatabase?.getAllCurrency(), apiResponse)
    }
}