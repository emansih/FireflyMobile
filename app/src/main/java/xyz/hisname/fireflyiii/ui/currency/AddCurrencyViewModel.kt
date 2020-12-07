package xyz.hisname.fireflyiii.ui.currency

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData

class AddCurrencyViewModel(application: Application): BaseViewModel(application) {

    private val currencyRepository = CurrencyRepository(
            AppDatabase.getInstance(application).currencyDataDao(),
            genericService()?.create(CurrencyService::class.java)
    )

    fun getCurrencyById(currencyId: Long): LiveData<List<CurrencyData>>{
        val currencyListLiveData = MutableLiveData<List<CurrencyData>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            currencyListLiveData.postValue(currencyRepository.getCurrencyById(currencyId))
            isLoading.postValue(false)
        }
        return currencyListLiveData
    }

    fun updateCurrency(name: String, code: String, symbol: String, decimalPlaces: String,
                       enabled: Boolean,default: Boolean): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            val updateCurrency = currencyRepository.updateCurrency(name, code, symbol, decimalPlaces, enabled, default)
            when {
                updateCurrency.response != null -> {
                    apiResponse.postValue(Pair(true, "Currency updated"))
                }
                updateCurrency.errorMessage != null -> {
                    apiResponse.postValue(Pair(false,updateCurrency.errorMessage))
                }
                updateCurrency.error != null -> {
                    apiResponse.postValue(Pair(false,updateCurrency.error.localizedMessage))
                }
                else -> {
                    apiResponse.postValue(Pair(false, "Error updating currency"))
                }
            }
            isLoading.postValue(false)
        }
        return apiResponse
    }

    fun addCurrency(name: String, code: String, symbol: String, decimalPlaces: String,
                    enabled: Boolean,default: Boolean): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            val updateCurrency = currencyRepository.addCurrency(name, code, symbol, decimalPlaces, enabled, default)
            when {
                updateCurrency.response != null -> {
                    apiResponse.postValue(Pair(true, "Currency updated"))
                }
                updateCurrency.errorMessage != null -> {
                    apiResponse.postValue(Pair(false,updateCurrency.errorMessage))
                }
                updateCurrency.error != null -> {
                    apiResponse.postValue(Pair(false,updateCurrency.error.localizedMessage))
                }
                else -> {
                    apiResponse.postValue(Pair(false, "Error updating currency"))
                }
            }
            isLoading.postValue(false)
        }
        return apiResponse
    }
}