package xyz.hisname.fireflyiii.repository.budget

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.api.BudgetService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.budget.BudgetData
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class BudgetViewModel(application: Application): BaseViewModel(application) {

    val repository: BudgetRepository
    private val availableBudgets by lazy { genericService()?.create(BudgetService::class.java) }
    private val budgetService by lazy { genericService()?.create(BudgetService::class.java) }
    private var currentMonthBudgetLimit = 0.toDouble()
    private var currentMonthBudgetValue: MutableLiveData<String> = MutableLiveData()

    init {
        val budgetDao = AppDatabase.getInstance(application).budgetDataDao()
        repository = BudgetRepository(budgetDao)
    }

    fun retrieveAllBudgetLimits(): LiveData<MutableList<BudgetData>> {
        loadRemoteLimit()
        return repository.allBudget
    }

    fun retrieveAllBudget(): LiveData<MutableList<BudgetData>>{
        loadRemoteBudget()
        return repository.allBudget
    }

    fun retrieveCurrentMonthBudget(): LiveData<String>{
        var availableBudget: MutableList<BudgetData>? = null
        loadRemoteLimit()
        currentMonthBudgetLimit = 0.toDouble()
        scope.async(Dispatchers.IO){
            availableBudget = repository.retrieveConstraintBudget(DateTimeUtil.getStartOfMonth(),
                    DateTimeUtil.getEndOfMonth())
        }.invokeOnCompletion {
            if(availableBudget.isNullOrEmpty()){
                currentMonthBudgetLimit = 0.toDouble()
            } else {
                availableBudget?.forEachIndexed { _, budgetData ->
                   currentMonthBudgetLimit += budgetData.budgetAttributes?.amount?.toDouble() ?: 0.toDouble()
                }
            }
            currentMonthBudgetValue.postValue(currentMonthBudgetLimit.toString())
        }
        return currentMonthBudgetValue
    }

    private fun loadRemoteBudget(){
        isLoading.value = true
        budgetService?.getAllBudget()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val networkData = response.body()
                if (networkData != null) {
                    for (pagination in 1..networkData.meta.pagination.total_pages) {
                        budgetService!!.getPaginatedBudget(pagination).enqueue(retrofitCallback({ respond ->
                            respond.body()?.budgetData?.forEachIndexed { _, budgetList ->
                                scope.launch(Dispatchers.IO) { repository.insertBudget(budgetList) }
                            }
                        }))
                    }
                }

            } else {
                val responseError = response.errorBody()
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    apiResponse.postValue(gson.message)
                }
            }
        })
        { throwable -> apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage)) })
        isLoading.value = false
    }

    private fun loadRemoteLimit(){
        isLoading.value = true
        availableBudgets?.getAllBudget()?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val networkData = response.body()
                if (networkData != null) {
                    for (pagination in 1..networkData.meta.pagination.total_pages) {
                        availableBudgets!!.getPaginatedBudget(pagination).enqueue(retrofitCallback({ respond ->
                            respond.body()?.budgetData?.forEachIndexed { _, budgetList ->
                                scope.launch(Dispatchers.IO) { repository.insertBudget(budgetList) }
                            }
                        }))
                    }
                }

            } else {
                val responseError = response.errorBody()
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    apiResponse.postValue(gson.message)
                }
            }
        })
        { throwable -> apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage)) })
        isLoading.value = false

    }
}