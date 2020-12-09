package xyz.hisname.fireflyiii.ui.account

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.account.AccountPageSource
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.account.DeleteAccountWorker

class ListAccountViewModel(application: Application): BaseViewModel(application) {

    private val accountService = genericService().create(AccountsService::class.java)
    private val accountsDataDao = AppDatabase.getInstance(getApplication()).accountDataDao()
    private val accountRepository = AccountRepository(accountsDataDao, accountService)

    fun getAccountList(accountType: String): LiveData<PagingData<AccountData>>{
        return Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
            AccountPageSource(accountType, accountsDataDao, accountService)
        }.flow.cachedIn(viewModelScope).asLiveData()
    }

    fun deleteAccountByName(accountId: String): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            val accountData = accountRepository.getAccountById(accountId.toLong())
            if(accountData.accountId != 0L){
                // Since onDraw() is being called multiple times, we check if the account exists locally in the DB.
                when (accountRepository.deleteAccountById(accountId.toLong())) {
                    HttpConstants.FAILED -> {
                        isDeleted.postValue(false)
                        DeleteAccountWorker.initPeriodicWorker(accountId.toLong(), getApplication())
                    }
                    HttpConstants.UNAUTHORISED -> {
                        isDeleted.postValue(false)
                    }
                    HttpConstants.NO_CONTENT_SUCCESS -> {
                        isDeleted.postValue(true)
                    }
                }
            }
        }
        return isDeleted
    }
}