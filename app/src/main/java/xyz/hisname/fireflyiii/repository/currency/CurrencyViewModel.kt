package xyz.hisname.fireflyiii.repository.currency

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.data.remote.api.CurrencyService
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.util.retrofitCallback

class CurrencyViewModel(application: Application) : BaseViewModel(application) {

    val currencyCode =  MutableLiveData<String>()
    val currencyDetails = MutableLiveData<String>()
    val repository: CurrencyRepository

    init {
        val currencyDataDao = AppDatabase.getInstance(application).currencyDataDao()
        repository = CurrencyRepository(currencyDataDao)

    }

    fun getCurrency(): LiveData<MutableList<CurrencyData>> {
        isLoading.value = true
        genericService()?.create(CurrencyService::class.java)?.getCurrency()?.enqueue(retrofitCallback({ response ->
            if(response.isSuccessful){
                val networkData = response.body()?.data
                networkData?.forEachIndexed { _, element ->
                    scope.launch(Dispatchers.IO) { repository.insertCurrency(element) }
                }
            } else {
                val responseError = response.errorBody()
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    apiResponse.postValue(errorBody)
                }
            }
        })
        { throwable ->  apiResponse.postValue(throwable.localizedMessage)})
        isLoading.value = false
        return repository.allCurrency
    }

    fun setCurrencyCode(code: String?) {
        currencyCode.value = code
    }

    fun setFullDetails(details: String?){
        currencyDetails.value = details
    }
}