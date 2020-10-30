package xyz.hisname.fireflyiii.repository.budget

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
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

    private val repository: BudgetRepository
    private val budgetService by lazy { genericService()?.create(BudgetService::class.java) }
    private var currentMonthBudgetValue: MutableLiveData<String> = MutableLiveData()
    val budgetName =  MutableLiveData<String>()
    private val spentDao by lazy { AppDatabase.getInstance(application).spentDataDao() }
    private val budgetLimitDao by lazy { AppDatabase.getInstance(application).budgetLimitDao() }
    private val budgetDao by lazy { AppDatabase.getInstance(application).budgetDataDao() }
    private val budgetListDao by lazy { AppDatabase.getInstance(application).budgetListDataDao() }

    init {
        repository = BudgetRepository(budgetDao, budgetListDao, spentDao, budgetLimitDao, budgetService)
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
                    val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorBody)
                    if(moshi == null){
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
        var budgetSpent = 0.0
        val currentMonthSpentValue: MutableLiveData<String> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            budgetSpent = repository.allActiveSpentList(currencyCode, DateTimeUtil.getStartOfMonth(),
                    DateTimeUtil.getEndOfMonth())
        }.invokeOnCompletion {
            currentMonthSpentValue.postValue(budgetSpent.toString())
        }
        return currentMonthSpentValue
    }

    fun retrieveSpentBudgetById(budgetName: String?, currencyCode: String): LiveData<String>{
        var budgetSpent = 0.0
        val currentMonthSpentValue: MutableLiveData<String> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            if(budgetName != null){
                budgetSpent = repository.getBudgetListByIdAndCurrencyCode(budgetName, currencyCode)
            }
        }.invokeOnCompletion {
            currentMonthSpentValue.postValue(budgetSpent.toString())
        }
        return currentMonthSpentValue
    }

    fun retrieveBudgetLimit(budgetName: String?, currencyCode: String, startOfMonth: String, endOfMonth: String): LiveData<Double>{
        var budgetLimit = 0.0
        val budgetLimitLiveData: MutableLiveData<Double> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            if(budgetName != null){
                budgetLimit = repository.getBudgetLimitByName(budgetName, currencyCode, startOfMonth, endOfMonth)
            }
        }.invokeOnCompletion {
            budgetLimitLiveData.postValue(budgetLimit)
        }
        return budgetLimitLiveData
    }
}