package xyz.hisname.fireflyiii.repository.summary

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.SimpleData
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.data.remote.firefly.api.SummaryService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.LocaleNumberParser
import xyz.hisname.fireflyiii.util.Version
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import kotlin.math.absoluteValue

class SummaryViewModel(application: Application): BaseViewModel(application) {

    val networthValue: MutableLiveData<Double> = MutableLiveData()
    val leftToSpendValue: MutableLiveData<Double> = MutableLiveData()
    private val accountDao by lazy { AppDatabase.getInstance(application).accountDataDao() }
    private val accountsService by lazy { genericService()?.create(AccountsService::class.java) }

    fun getBasicSummary(startDate: String, endDate: String, currencyCode: String, apiVersion: String){
        if(Version(apiVersion).compareTo(Version("0.10.0")) == 1 ||
            Version(apiVersion) == Version("0.10.0")) {
            val simpleData = SimpleData(PreferenceManager.getDefaultSharedPreferences(getApplication()))
            val summaryService = genericService()?.create(SummaryService::class.java)
            summaryService?.getBasicSummary(DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(),
                    currencyCode)?.enqueue(retrofitCallback({ response ->
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    // so dirty I went to take a shower after writing this code
                    val netWorth = JSONObject(responseBody)
                            .getJSONObject("net-worth-in-$currencyCode")
                            .getDouble("monetary_value")
                    simpleData.networthValue = netWorth.toString()
                    val leftToSpend =  JSONObject(responseBody)
                            .getJSONObject("left-to-spend-in-$currencyCode")
                            .getDouble("monetary_value")
                    simpleData.leftToSpend = leftToSpend.toString()
                    leftToSpendValue.postValue(leftToSpend)
                    networthValue.postValue(netWorth)
                } else {
                    if (simpleData.networthValue.isBlank()) {
                        getOldNetworth(currencyCode)
                    } else {
                        networthValue.postValue(simpleData.networthValue.toDouble())
                    }
                    leftToSpendValue.postValue(simpleData.leftToSpend.toDouble())
                }
            }))
        } else {
            getOldNetworth(currencyCode)
        }
    }

    private fun getOldNetworth(currencyCode: String){
        var currentBalance = 0.0
        val repository = AccountRepository(accountDao, accountsService)
        viewModelScope.launch(Dispatchers.IO) {
            currentBalance = repository.retrieveAccountWithCurrencyCodeAndNetworth(currencyCode)
        }.invokeOnCompletion {
            networthValue.postValue(LocaleNumberParser.parseDecimal(currentBalance, getApplication()).absoluteValue)
        }
    }
}