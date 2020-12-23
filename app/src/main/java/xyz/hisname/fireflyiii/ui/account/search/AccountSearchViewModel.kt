package xyz.hisname.fireflyiii.ui.account.search

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.account.AccountPageSource
import xyz.hisname.fireflyiii.repository.account.AccountSearchPageSource
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData

class AccountSearchViewModel(application: Application): BaseViewModel(application) {

    private val accountDao = AppDatabase.getInstance(application).accountDataDao()
    private val accountService = genericService().create(AccountsService::class.java)

    val accountName = MutableLiveData<String>()

    fun searchAccount(query: String, accountType: String): LiveData<PagingData<AccountData>>{
        return Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
            AccountSearchPageSource(query, accountType, accountDao, accountService)
        }.flow.cachedIn(viewModelScope).asLiveData()
    }

    fun getAccountList(accountType: String): LiveData<PagingData<AccountData>>{
        return Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
            AccountPageSource(accountType, accountDao, accountService)
        }.flow.cachedIn(viewModelScope).asLiveData()
    }
}