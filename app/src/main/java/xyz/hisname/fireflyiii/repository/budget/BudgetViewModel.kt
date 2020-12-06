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
    private val spentDao by lazy { AppDatabase.getInstance(application).spentDataDao() }
    private val budgetLimitDao by lazy { AppDatabase.getInstance(application).budgetLimitDao() }
    private val budgetDao by lazy { AppDatabase.getInstance(application).budgetDataDao() }
    private val budgetListDao by lazy { AppDatabase.getInstance(application).budgetListDataDao() }

    init {
        repository = BudgetRepository(budgetDao, budgetListDao, spentDao, budgetLimitDao, budgetService)
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
}