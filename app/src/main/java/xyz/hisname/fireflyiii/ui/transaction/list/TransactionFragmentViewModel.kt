package xyz.hisname.fireflyiii.ui.transaction.list

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.repository.transaction.TransactionPagingSource

class TransactionFragmentViewModel(application: Application): BaseViewModel(application) {

    private val transactionService by lazy { genericService()?.create(TransactionService::class.java) }
    private val transactionDataDao = AppDatabase.getInstance(application).transactionDataDao()

    fun getTransactionList(startDate: String?, endDate: String?,
                           transactionType: String): LiveData<PagingData<Transactions>> {
        return Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)) {
            TransactionPagingSource(transactionService, transactionDataDao, startDate, endDate, transactionType)
        }.flow.cachedIn(viewModelScope).asLiveData()
    }
}