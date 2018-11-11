package xyz.hisname.fireflyiii.repository.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.TransactionService
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.Response
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionSuccessModel
import xyz.hisname.fireflyiii.util.retrofitCallback
import java.util.*

class TransactionViewModel(application: Application) : AndroidViewModel(application){

    private val transactionDatabase by lazy { AppDatabase.getInstance(application)?.transactionDataDao() }
    private val apiResponse: MediatorLiveData<ApiResponses<TransactionModel>> = MediatorLiveData()

    fun getTransactions(baseUrl: String?, accessToken: String?, start: String?, end: String?, type: String):
            Response<TransactionData, ApiResponses<TransactionModel>> {
        val transaction: MutableLiveData<ApiResponses<TransactionModel>> = MutableLiveData()
        val transactionService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(TransactionService::class.java)
        transactionService?.getAllTransactions(start, end, type)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                response.body()?.data?.forEachIndexed { _, element ->
                    GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
                        transactionDatabase?.insert(element)
                    }
                }
                transaction.value = ApiResponses(response.body())
            }
        })
        { throwable ->  transaction.value = ApiResponses(throwable)})
        apiResponse.addSource(transaction) { apiResponse.value = it }
        return when {
            Objects.equals("all", type) -> Response(transactionDatabase?.getRecentTransactions(5), apiResponse)
            start.isNullOrBlank() or end.isNullOrBlank() -> Response(transactionDatabase?.getTransaction(type), apiResponse)
            else -> Response(transactionDatabase?.getTransaction(start, end, type), apiResponse)
        }
    }

    fun getTransaction(baseUrl: String?, accessToken: String?, start: String?, end: String?, type: String):
            LiveData<ApiResponses<TransactionModel>>{
        val transaction: MutableLiveData<ApiResponses<TransactionModel>> = MutableLiveData()
        val apiResponse: MediatorLiveData<ApiResponses<TransactionModel>> = MediatorLiveData()
        val transactionService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(TransactionService::class.java)
        transactionService?.getAllTransactions(start, end, type)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                transaction.postValue(ApiResponses(response.body()))
            }
        })
        { throwable ->  transaction.postValue(ApiResponses(throwable))})
        apiResponse.addSource(transaction) { apiResponse.value = it }
        return apiResponse
    }

    fun addTransaction(baseUrl: String?, accessToken: String?, type: String, description: String,
                       date: String, piggyBankName: String?, billName: String?, amount: String,
                       sourceName: String?, destinationName: String?, currencyName: String,
                       category: String?): LiveData<ApiResponses<TransactionSuccessModel>>{
        val transaction: MutableLiveData<ApiResponses<TransactionSuccessModel>> = MutableLiveData()
        val apiResponse: MediatorLiveData<ApiResponses<TransactionSuccessModel>> = MediatorLiveData()
        val transactionService = RetrofitBuilder.getClient(baseUrl,accessToken)?.create(TransactionService::class.java)
        transactionService?.addTransaction(convertString(type),description,date,piggyBankName,billName,
                amount,sourceName,destinationName,currencyName, category)?.enqueue(retrofitCallback({ response ->
            val errorBody = response.errorBody()
            var errorBodyMessage = ""
            if(errorBody != null) {
                errorBodyMessage = String(errorBody.bytes())
                val gson = Gson().fromJson(errorBodyMessage, ErrorModel::class.java)
                when {
                    gson.errors.transactions_currency != null -> {
                        if (gson.errors.transactions_currency.contains("is required")) {
                            errorBodyMessage = "Currency Code Required"
                        } else {
                            errorBodyMessage = "Invalid Currency Code"
                        }
                    }
                    gson.errors.bill_name != null -> errorBodyMessage = "Invalid Bill Name"
                    gson.errors.piggy_bank_name != null -> errorBodyMessage = "Invalid Piggy Bank Name"
                    gson.errors.transactions_destination_name != null -> errorBodyMessage = "Invalid Destination Account"
                    gson.errors.transactions_source_name != null -> errorBodyMessage = "Invalid Source Account"
                    gson.errors.transaction_destination_id != null -> errorBodyMessage = gson.errors.transaction_destination_id[0]
                    else -> errorBodyMessage = "Error occurred while saving transaction"
                }
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

    private fun convertString(type: String) = type.substring(0,1).toLowerCase() + type.substring(1).toLowerCase()

}