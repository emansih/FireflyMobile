package xyz.hisname.fireflyiii.repository.transaction

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.data.remote.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.util.retrofitCallback

class TransactionsViewModel(application: Application): BaseViewModel(application) {

    val repository: TransactionRepository

    init {
        val transactionDataDao = AppDatabase.getInstance(application).transactionDataDao()
        repository = TransactionRepository(transactionDataDao)
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

    fun getRecentTransaction(limit: Int): LiveData<MutableList<TransactionData>>{
        isLoading.value = true
        var recentData: MutableList<TransactionData> = arrayListOf()
        val data: MutableLiveData<MutableList<TransactionData>> = MutableLiveData()
        val transactionService = RetrofitBuilder.getClient(AppPref(getApplication()).baseUrl,
                AppPref(getApplication()).accessToken)?.create(TransactionService::class.java)
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

    private fun loadRemoteData(startDate: String?, endDate: String?, source: String){
        isLoading.value = true
        val transactionService = RetrofitBuilder.getClient(AppPref(getApplication()).baseUrl,
                AppPref(getApplication()).accessToken)?.create(TransactionService::class.java)
        transactionService?.getAllTransactions(startDate, endDate, source)?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful){
                val networkData = response.body()
                if(networkData != null){
                    for(pagination in 1..networkData.meta.pagination.total_pages){
                        transactionService.getPaginatedTransactions(startDate,endDate, source, pagination).enqueue(retrofitCallback({
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