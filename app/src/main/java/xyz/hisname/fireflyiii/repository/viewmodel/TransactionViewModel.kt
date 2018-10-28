package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.TransactionService
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.BaseResponse
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionModel
import xyz.hisname.fireflyiii.repository.models.transaction.sucess.TransactionSucessModel
import xyz.hisname.fireflyiii.util.retrofitCallback
import java.util.*

class TransactionViewModel(application: Application) : AndroidViewModel(application){

    private val transactionDatabase by lazy { AppDatabase.getInstance(application)?.transactionDataDao() }
    val apiResponse: MediatorLiveData<ApiResponses<TransactionModel>> = MediatorLiveData()

    fun getTransactions(baseUrl: String?, accessToken: String?, start: String?, end: String?, type: String):
            BaseResponse<TransactionData, ApiResponses<TransactionModel>> {
        val transaction: MutableLiveData<ApiResponses<TransactionModel>> = MutableLiveData()
        val transactionService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(TransactionService::class.java)
        transactionService?.getAllTransactions(start, end, type)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                response.body()?.data?.forEachIndexed { _, element ->
                    GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT, null, {
                        transactionDatabase?.addTransaction(element)
                    })
                }
                transaction.value = ApiResponses(response.body())
            }
        })
        { throwable ->  transaction.value = ApiResponses(throwable)})
        apiResponse.addSource(transaction) { apiResponse.value = it }
        return if(Objects.equals("all", type)){
            BaseResponse(transactionDatabase?.getRecentTransactions(5), apiResponse)
        } else if(start.isNullOrBlank() or end.isNullOrBlank()){
            BaseResponse(transactionDatabase?.getTransaction(type), apiResponse)
        } else {
            BaseResponse(transactionDatabase?.getTransaction(start, end, type), apiResponse)
        }
    }

    fun addTransaction(baseUrl: String?, accessToken: String?, type: String, description: String,
                       date: String, piggyBankName: String?, billName: String?, amount: String,
                       sourceName: String?, destinationName: String?, currencyName: String): LiveData<ApiResponses<TransactionSucessModel>>{
        val transaction: MutableLiveData<ApiResponses<TransactionSucessModel>> = MutableLiveData()
        val apiResponse: MediatorLiveData<ApiResponses<TransactionSucessModel>> = MediatorLiveData()
        val transactionService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(TransactionService::class.java)
        transactionService?.addTransaction(type,description,date,piggyBankName,billName,
                amount,sourceName,destinationName,currencyName)?.enqueue(retrofitCallback({ response ->
            val errorBody = response.errorBody()
            var errorBodyMessage = ""
            if(errorBody != null){
                errorBodyMessage = String(errorBody.bytes())
            }
            if (response.isSuccessful) {
                transaction.postValue(ApiResponses(response.body()))
            } else {
                transaction.postValue(ApiResponses(errorBodyMessage))
            }
        })
        { throwable ->  transaction.value = ApiResponses(throwable)})
        apiResponse.addSource(transaction) { apiResponse.value = it }
        return apiResponse
    }
}