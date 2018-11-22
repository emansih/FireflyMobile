package xyz.hisname.fireflyiii.repository.transaction

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionSuccessModel
import xyz.hisname.fireflyiii.util.retrofitCallback

class TransactionsViewModel(application: Application): BaseViewModel(application) {

    val repository: TransactionRepository
    private val transactionService by lazy { genericService()?.create(TransactionService::class.java) }

    init {
        val transactionDataDao = AppDatabase.getInstance(application).transactionDataDao()
        repository = TransactionRepository(transactionDataDao)
    }

    // PLACE HOLDER. DO NOT CALL THIS FUNCTION FROM ANYWHERE EXCEPT ONBOARDING!!!
    fun getAllData(startDate: String?, endDate: String?): LiveData<Boolean> {
        val isLoaded: MutableLiveData<Boolean> = MutableLiveData()
        isLoaded.value = false
        loadRemoteData(startDate, endDate, "all")
        isLoaded.value = true
        return isLoaded
    }

    fun getWithdrawalList(startDate: String?, endDate: String?): LiveData<MutableList<TransactionData>> {
        loadRemoteData(startDate, endDate, "withdrawal")
        return repository.withdrawalList(startDate, endDate)
    }

    fun getDepositList(startDate: String?, endDate: String?): LiveData<MutableList<TransactionData>> {
        loadRemoteData(startDate, endDate, "deposit")
        return repository.depositList(startDate,endDate)
    }

    fun getTransferList(startDate: String?, endDate: String?): LiveData<MutableList<TransactionData>> {
        loadRemoteData(startDate, endDate, "transfer")
        return repository.transferList(startDate,endDate)
    }

    fun getRecentTransaction(limit: Int): LiveData<MutableList<TransactionData>>{
        isLoading.value = true
        var recentData: MutableList<TransactionData> = arrayListOf()
        val data: MutableLiveData<MutableList<TransactionData>> = MutableLiveData()
        transactionService?.getAllTransactions("","", "all")?.enqueue(retrofitCallback({
            response ->
            if (response.isSuccessful) {
                val networkData = response.body()
                networkData?.data?.forEachIndexed{ _, data ->
                    scope.launch(Dispatchers.IO) { repository.insertTransaction(data)}
                }
            }
        })
        { throwable ->  })
        scope.async(Dispatchers.IO){
            recentData = repository.recentTransactions(limit)
        }.invokeOnCompletion {
            data.postValue(recentData)
            isLoading.postValue(false)
        }
        return data
    }

    fun getWithdrawal(startDate: String?, endDate: String?): LiveData<MutableList<TransactionData>> {
        var withdrawData: MutableList<TransactionData> = arrayListOf()
        val data: MutableLiveData<MutableList<TransactionData>> = MutableLiveData()
        loadRemoteData(startDate, endDate, "withdrawal")
        scope.async(Dispatchers.IO) {
            withdrawData = repository.allWithdrawal(startDate, endDate)
        }.invokeOnCompletion {
            data.postValue(withdrawData)
        }
        isLoading.value = false
        return data
    }

    fun getDeposit(startDate: String?, endDate: String?): LiveData<MutableList<TransactionData>> {
        var depositData: MutableList<TransactionData> = arrayListOf()
        val data: MutableLiveData<MutableList<TransactionData>> = MutableLiveData()
        loadRemoteData(startDate, endDate, "deposit")
        scope.async(Dispatchers.IO) {
            depositData = repository.allDeposit(startDate, endDate)
        }.invokeOnCompletion {
            data.postValue(depositData)
        }
        isLoading.value = false
        return data
    }

    fun addTransaction(type: String, description: String,
                       date: String, piggyBankName: String?, billName: String?, amount: String,
                       sourceName: String?, destinationName: String?, currencyName: String,
                       category: String?): LiveData<ApiResponses<TransactionSuccessModel>>{
        val transaction: MutableLiveData<ApiResponses<TransactionSuccessModel>> = MutableLiveData()
        val apiResponse: MediatorLiveData<ApiResponses<TransactionSuccessModel>> = MediatorLiveData()
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
        { throwable ->  transaction.value = ApiResponses(throwable) })
        apiResponse.addSource(transaction) { apiResponse.value = it }
        return apiResponse
    }

    private fun convertString(type: String) = type.substring(0,1).toLowerCase() + type.substring(1).toLowerCase()

    private fun loadRemoteData(startDate: String?, endDate: String?, source: String){
        isLoading.value = true
        transactionService?.getAllTransactions(startDate, endDate, source)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful){
                val networkData = response.body()
                if(networkData != null){
                    for(pagination in 1..networkData.meta.pagination.total_pages){
                        transactionService!!.getPaginatedTransactions(startDate,endDate, source, pagination).enqueue(retrofitCallback({
                            respond ->
                            respond.body()?.data?.forEachIndexed{ _, transactionPagination ->
                                scope.launch(Dispatchers.IO) { repository.insertTransaction(transactionPagination)}
                            }
                        }))
                    }
                }

            } else {
                val responseError = response.errorBody()
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    apiResponse.postValue(errorBody)
                }
            }
        })
        { throwable -> apiResponse.postValue(throwable.localizedMessage) })
        isLoading.value = false
    }

}