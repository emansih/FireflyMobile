package xyz.hisname.fireflyiii.repository.currency

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData

class CurrencyViewModel(application: Application) : BaseViewModel(application) {

    val repository: CurrencyRepository

    private val currencyService by lazy { genericService()?.create(CurrencyService::class.java) }

    init {
        val currencyDataDao = AppDatabase.getInstance(application).currencyDataDao()
        repository = CurrencyRepository(currencyDataDao, currencyService)

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

    fun getDefaultCurrency(): LiveData<List<CurrencyData>>{
        val currencyLiveData: MutableLiveData<List<CurrencyData>> = MutableLiveData()
        var currencyData: List<CurrencyData> = listOf()
        viewModelScope.launch(Dispatchers.IO){
            currencyData = repository.defaultCurrency()
        }.invokeOnCompletion {
            currencyLiveData.postValue(currencyData)
        }
        return currencyLiveData
    }
}