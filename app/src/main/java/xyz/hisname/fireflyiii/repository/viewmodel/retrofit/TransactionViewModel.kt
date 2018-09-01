package xyz.hisname.fireflyiii.repository.viewmodel.retrofit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionApiResponse
import xyz.hisname.fireflyiii.util.retrofitCallback

class TransactionViewModel:ViewModel() {


    fun getTransactions(baseUrl: String?, accessToken: String?, start: String, end: String, type: String):
            LiveData<TransactionApiResponse>{
        val apiResponse: MediatorLiveData<TransactionApiResponse> = MediatorLiveData()
        val transaction: MutableLiveData<TransactionApiResponse> = MutableLiveData()
        val transService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(TransactionService::class.java)
        transService?.getAllTransactions(start, end, type)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                transaction.value = TransactionApiResponse(response.body())
            }
        })
        { throwable ->  transaction.value = TransactionApiResponse(throwable)})
        apiResponse.addSource(transaction) { apiResponse.value = it }
        return apiResponse
    }

}