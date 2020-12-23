package xyz.hisname.fireflyiii.ui.transaction.search

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.transaction.TransactionSearchPagingSource

class DescriptionViewModel(application: Application): BaseViewModel(application) {

    private val transactionService = genericService().create(TransactionService::class.java)
    private val transactionDao = AppDatabase.getInstance(application).transactionDataDao()
    val transactionName = MutableLiveData<String>()

    fun searchTransactionName(searchName: String) = Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
        TransactionSearchPagingSource(transactionService, transactionDao, searchName)
    }.flow.cachedIn(viewModelScope).asLiveData()

    fun getAllDescription() = Pager(PagingConfig(pageSize = 100)){
        TransactionSearchPagingSource(transactionService, transactionDao, "")
    }.flow.cachedIn(viewModelScope).asLiveData()
}