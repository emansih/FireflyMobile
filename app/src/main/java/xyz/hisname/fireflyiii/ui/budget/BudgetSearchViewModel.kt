package xyz.hisname.fireflyiii.ui.budget

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.BudgetService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.budget.BudgetPagingSource
import xyz.hisname.fireflyiii.repository.budget.BudgetSearchPagingSource

class BudgetSearchViewModel(application: Application): BaseViewModel(application) {

    private val budgetDao = AppDatabase.getInstance(application).budgetListDataDao()
    private val budgetService = genericService()?.create(BudgetService::class.java)

    val budgetName = MutableLiveData<String>()

    fun getAllBudget() =
        Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)) {
            BudgetPagingSource(budgetDao, budgetService)
        }.flow.cachedIn(viewModelScope).asLiveData(Dispatchers.IO)

    fun searchBudget(searchName: String) = Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)) {
        BudgetSearchPagingSource(searchName, budgetDao, budgetService)
    }.flow.cachedIn(viewModelScope).asLiveData(Dispatchers.IO)
}