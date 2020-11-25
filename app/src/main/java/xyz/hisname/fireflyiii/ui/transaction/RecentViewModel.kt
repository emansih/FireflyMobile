package xyz.hisname.fireflyiii.ui.transaction

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.repository.transaction.TransactionLimitSource

class RecentViewModel(application: Application): BaseViewModel(application) {

    private val transactionService = genericService()?.create(TransactionService::class.java)
    private val transactionDataDao = AppDatabase.getInstance(application).transactionDataDao()

    fun getRecentTransaction(limit: Int): LiveData<PagingData<Transactions>> {
        return Pager(PagingConfig(limit)) {
            TransactionLimitSource(limit, transactionDataDao, transactionService)
        }.flow.cachedIn(viewModelScope).asLiveData()
    }

}