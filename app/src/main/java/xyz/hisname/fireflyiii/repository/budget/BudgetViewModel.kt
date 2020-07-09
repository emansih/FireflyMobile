package xyz.hisname.fireflyiii.repository.budget

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.BudgetService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.budget.BudgetData
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.network.NetworkErrors
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import java.math.BigDecimal

class BudgetViewModel(application: Application): BaseViewModel(application) {

    val repository: BudgetRepository
    private val budgetService by lazy { genericService()?.create(BudgetService::class.java) }
    private var currentMonthBudgetValue: MutableLiveData<String> = MutableLiveData()
    private var currentMonthSpentValue: MutableLiveData<String> = MutableLiveData()
    val spentBudgetLoader: MutableLiveData<Boolean> = MutableLiveData()
    val budgetName =  MutableLiveData<String>()
    private val spentDao by lazy { AppDatabase.getInstance(application).spentDataDao() }

    init {
        val budgetDao = AppDatabase.getInstance(application).budgetDataDao()
        val budgetListDao = AppDatabase.getInstance(application).budgetListDataDao()
        repository = BudgetRepository(budgetDao, budgetListDao, spentDao)
    }

    fun retrieveAllBudgetLimits(pageNumber: Int): LiveData<MutableList<BudgetListData>> {
        isLoading.value = true
        var budgetListData: MutableList<BudgetListData> = arrayListOf()
        val data: MutableLiveData<MutableList<BudgetListData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            budgetListData = repository.allBudgetList(pageNumber)
        }.invokeOnCompletion {
            isLoading.postValue(false)
            data.postValue(budgetListData)
        }
        return data
    }

    fun getBudgetByName(budgetName: String): LiveData<MutableList<BudgetListData>>{
        var budgetListData: MutableList<BudgetListData> = arrayListOf()
        val data: MutableLiveData<MutableList<BudgetListData>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            budgetListData = repository.searchBudgetByName("$budgetName*")
        }.invokeOnCompletion {
            data.postValue(budgetListData)
        }
        return data
    }

    fun postBudgetName(details: String?){
        budgetName.value = details
    }

    fun retrieveCurrentMonthBudget(currencyCode: String): LiveData<String>{
        val availableBudget: MutableList<BudgetData> = arrayListOf()
        var currencyMonthBud: BigDecimal? = 0.toBigDecimal()
        budgetService?.getAllBudget()?.enqueue(retrofitCallback({ response ->
            val responseError = response.errorBody()
            if (response.isSuccessful) {
                val networkData = response.body()
                if (networkData != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.deleteAllBudget()
                    }.invokeOnCompletion {
                        if (networkData.meta.pagination.current_page != networkData.meta.pagination.total_pages) {
                            for (pagination in 2..networkData.meta.pagination.total_pages) {
                                budgetService?.getPaginatedBudget(pagination)?.enqueue(retrofitCallback({ respond ->
                                    respond.body()?.budgetData?.forEachIndexed { _, budgetList ->
                                        availableBudget.add(budgetList)
                                    }
                                }))
                            }
                        }
                        networkData.budgetData.forEachIndexed { _, budgetData ->
                            availableBudget.add(budgetData)
                        }
                        viewModelScope.launch(Dispatchers.IO){
                            availableBudget.forEachIndexed { _, budgetData ->
                                repository.insertBudget(budgetData)
                            }
                        }.invokeOnCompletion {
                            viewModelScope.launch(Dispatchers.IO){
                                currencyMonthBud = if(repository.retrieveConstraintBudgetWithCurrency(DateTimeUtil.getStartOfMonth(),
                                                DateTimeUtil.getEndOfMonth(), currencyCode) != null){
                                    repository.retrieveConstraintBudgetWithCurrency(DateTimeUtil.getStartOfMonth(),
                                            DateTimeUtil.getEndOfMonth(), currencyCode)
                                } else {
                                    0.toBigDecimal()
                                }
                            }.invokeOnCompletion {
                                currentMonthBudgetValue.postValue(currencyMonthBud.toString())
                            }
                        }
                    }
                }
            } else {
                if (responseError != null) {
                    val errorBody = String(responseError.bytes())
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    if(gson == null){
                        apiResponse.postValue("Error Loading Data")
                    } else {
                        apiResponse.postValue(errorBody)
                    }
                }
                viewModelScope.launch(Dispatchers.IO){
                    currencyMonthBud = if(repository.retrieveConstraintBudgetWithCurrency(DateTimeUtil.getStartOfMonth(),
                                    DateTimeUtil.getEndOfMonth(), currencyCode) != null){
                        repository.retrieveConstraintBudgetWithCurrency(DateTimeUtil.getStartOfMonth(),
                                DateTimeUtil.getEndOfMonth(), currencyCode)
                    } else {
                        0.toBigDecimal()
                    }
                }.invokeOnCompletion {
                    currentMonthBudgetValue.postValue(currencyMonthBud.toString())
                }
            }
        })
        { throwable ->
            viewModelScope.launch(Dispatchers.IO){
                currencyMonthBud = if(repository.retrieveConstraintBudgetWithCurrency(DateTimeUtil.getStartOfMonth(),
                                DateTimeUtil.getEndOfMonth(), currencyCode) != null){
                    repository.retrieveConstraintBudgetWithCurrency(DateTimeUtil.getStartOfMonth(),
                            DateTimeUtil.getEndOfMonth(), currencyCode)
                } else {
                    0.toBigDecimal()
                }
            }.invokeOnCompletion {
                currentMonthBudgetValue.postValue(currencyMonthBud.toString())
            }
            apiResponse.postValue(NetworkErrors.getThrowableMessage(throwable.localizedMessage))
        })
        return currentMonthBudgetValue
    }

    fun retrieveSpentBudget(currencyCode: String): LiveData<String>{
        spentBudgetLoader.value = true
        var budgetListData: MutableList<BudgetListData> = arrayListOf()
        var budgetSpent = 0.0
        budgetService?.getPaginatedSpentBudget(1, DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth())?.enqueue(retrofitCallback({ response ->
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    val networkData = responseBody.data
                    budgetListData.addAll(networkData)
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.deleteBudgetList()
                    }.invokeOnCompletion {
                        if (responseBody.meta.pagination.current_page != responseBody.meta.pagination.total_pages) {
                            for (pagination in 2..responseBody.meta.pagination.total_pages) {
                                budgetService?.getPaginatedSpentBudget(pagination, DateTimeUtil.getStartOfMonth(),
                                        DateTimeUtil.getEndOfMonth())?.enqueue(retrofitCallback({ respond ->
                                    respond.body()?.data?.forEachIndexed { _, budgetList ->
                                        budgetListData.add(budgetList)
                                    }
                                }))
                            }
                        }
                        viewModelScope.launch(Dispatchers.IO) {
                            budgetListData.forEachIndexed { _, data ->
                                repository.insertBudgetList(data)
                            }
                        }.invokeOnCompletion {
                            viewModelScope.launch(Dispatchers.IO) {
                                budgetSpent = repository.allActiveSpentList(currencyCode)
                            }.invokeOnCompletion {
                                spentBudgetLoader.postValue(false)
                                currentMonthSpentValue.postValue(budgetSpent.toString())
                            }
                        }
                    }
                }
            }
        })
        { throwable ->
            viewModelScope.launch(Dispatchers.IO) {
                budgetSpent = repository.allActiveSpentList(currencyCode)
            }.invokeOnCompletion {
                currentMonthSpentValue.postValue(budgetSpent.toString())
                spentBudgetLoader.postValue(false)
            }
        })
        return currentMonthSpentValue
    }
}