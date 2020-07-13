package xyz.hisname.fireflyiii.repository.summary

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import org.json.JSONObject
import xyz.hisname.fireflyiii.data.local.pref.SimpleData
import xyz.hisname.fireflyiii.data.remote.firefly.api.SummaryService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class SummaryViewModel(application: Application): BaseViewModel(application) {

    val networthValue: MutableLiveData<String> = MutableLiveData()
    val leftToSpendValue: MutableLiveData<String> = MutableLiveData()
    val balanceValue: MutableLiveData<String> = MutableLiveData()
    val earnedValue: MutableLiveData<String> = MutableLiveData()
    val spentValue: MutableLiveData<String> = MutableLiveData()
    val billsToPay: MutableLiveData<String> = MutableLiveData()
    val billsPaid: MutableLiveData<String> = MutableLiveData()
    val leftToSpendDay: MutableLiveData<String> = MutableLiveData()

    fun getBasicSummary(startDate: String, endDate: String, currencyCode: String){
            val simpleData = SimpleData(PreferenceManager.getDefaultSharedPreferences(getApplication()))
            val summaryService = genericService()?.create(SummaryService::class.java)
            summaryService?.getBasicSummary(startDate, endDate,
                    currencyCode)?.enqueue(retrofitCallback({ response ->
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    // so dirty I went to take a shower after writing this code
                    val netWorth = try {
                        JSONObject(responseBody)
                                .getJSONObject("net-worth-in-$currencyCode")
                                .getString("value_parsed")
                    } catch (exception: Exception){
                        "0.0"
                    }
                    simpleData.networthValue = netWorth
                    val leftToSpend = try {
                        JSONObject(responseBody)
                                .getJSONObject("left-to-spend-in-$currencyCode")
                                .getString("value_parsed")
                    } catch (exception: Exception){
                        "0.0"
                    }
                    simpleData.leftToSpend = leftToSpend

                    val balance = try {
                        JSONObject(responseBody)
                                .getJSONObject("balance-in-$currencyCode")
                                .getString("value_parsed")
                    } catch(exception: Exception){
                        "0.0"
                    }
                    simpleData.balance = balance

                    val earned = try {
                        JSONObject(responseBody)
                                .getJSONObject("earned-in-$currencyCode")
                                .getString("value_parsed")
                    } catch(exception: Exception){
                        "0.0"
                    }
                    simpleData.earned = earned

                    val spent = try {
                        JSONObject(responseBody)
                                .getJSONObject("spent-in-$currencyCode")
                                .getString("value_parsed")
                    } catch(exception: Exception){
                        "0.0"
                    }
                    simpleData.spent = spent

                    val unPaidBills = try {
                        JSONObject(responseBody)
                                .getJSONObject("bills-unpaid-in-$currencyCode")
                                .getString("value_parsed")
                    } catch(exception: Exception){
                        "0.0"
                    }
                    simpleData.unPaidBills = unPaidBills

                    val paidBills = try {
                        JSONObject(responseBody)
                                .getJSONObject("bills-paid-in-$currencyCode")
                                .getString("value_parsed")
                    } catch(exception: Exception){
                        "0.0"
                    }
                    simpleData.paidBills = paidBills

                    val leftToSpendPerDay = try {
                        JSONObject(responseBody)
                                .getJSONObject("left-to-spend-in-$currencyCode")
                                .getString("sub_title")
                    } catch(exception: Exception){
                        "0.0"
                    }
                    val formattedText = leftToSpendPerDay.replace("Left to spend per day: ", "")
                    simpleData.leftToSpendPerDay = formattedText
                    networthValue.postValue(simpleData.networthValue)
                    leftToSpendValue.postValue(simpleData.leftToSpend)
                    balanceValue.postValue(simpleData.balance)
                    earnedValue.postValue(simpleData.earned)
                    spentValue.postValue(simpleData.spent)
                    billsToPay.postValue(simpleData.unPaidBills)
                    billsPaid.postValue(simpleData.paidBills)
                    leftToSpendDay.postValue(simpleData.leftToSpendPerDay)
                } else {
                    networthValue.postValue(simpleData.networthValue)
                    leftToSpendValue.postValue(simpleData.leftToSpend)
                    balanceValue.postValue(simpleData.balance)
                    earnedValue.postValue(simpleData.earned)
                    spentValue.postValue(simpleData.spent)
                    billsToPay.postValue(simpleData.unPaidBills)
                    billsPaid.postValue(simpleData.paidBills)
                    leftToSpendDay.postValue(simpleData.leftToSpendPerDay)
                }
            })
            { throwable ->
                networthValue.postValue(simpleData.networthValue)
                leftToSpendValue.postValue(simpleData.leftToSpend)
                balanceValue.postValue(simpleData.balance)
                earnedValue.postValue(simpleData.earned)
                spentValue.postValue(simpleData.spent)
                billsToPay.postValue(simpleData.unPaidBills)
                billsPaid.postValue(simpleData.paidBills)
                leftToSpendDay.postValue(simpleData.leftToSpendPerDay)
            })

    }
}