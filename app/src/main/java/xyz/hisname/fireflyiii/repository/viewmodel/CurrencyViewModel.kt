package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.CurrencyService
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyApiResponse
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.util.retrofitCallback

class CurrencyViewModel(application: Application) : AndroidViewModel(application) {

    private val currencyDatabase by lazy { AppDatabase.getInstance(application)?.currencyDataDao() }
    private var  currencyService: CurrencyService? = null


    fun getCurrency(baseUrl: String, accessToken: String): CurrencyResponse{
        val apiResponse = MediatorLiveData<CurrencyApiResponse>()
        val billResponse: MutableLiveData<CurrencyApiResponse> = MutableLiveData()
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
                billResponse.postValue(CurrencyApiResponse(errorBody))
            }
        })
        { throwable ->  billResponse.postValue(CurrencyApiResponse(throwable))})
        apiResponse.addSource(billResponse) {
            apiResponse.value = it
        }
        return CurrencyResponse(currencyDatabase?.getAllCurrency(), apiResponse)
    }

}
data class CurrencyResponse(val databaseData: LiveData<MutableList<CurrencyData>>?, val apiResponse: MediatorLiveData<CurrencyApiResponse>)