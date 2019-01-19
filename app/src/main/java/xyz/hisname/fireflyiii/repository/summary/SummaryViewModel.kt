package xyz.hisname.fireflyiii.repository.summary

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xyz.hisname.fireflyiii.data.remote.api.SummaryService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class SummaryViewModel(application: Application): BaseViewModel(application) {


    private val summaryService by lazy { genericService()?.create(SummaryService::class.java) }

    fun getSummary(startDate: String, endDate: String): LiveData<String>{
        val netWorthData: MutableLiveData<String> = MutableLiveData()
        isLoading.value = true
        summaryService?.getSummaryData(startDate, endDate)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val responseBody = response.body()
                netWorthData.postValue(responseBody?.value_parsed)
            }
        })
        { throwable -> apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage)) })
        return netWorthData
    }

}