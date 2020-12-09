package xyz.hisname.fireflyiii.repository.account

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.account.DeleteAccountWorker


class AccountsViewModel(application: Application): BaseViewModel(application){

    val repository: AccountRepository
    private val accountsService by lazy { genericService().create(AccountsService::class.java) }

    init {
        val accountDao = AppDatabase.getInstance(application).accountDataDao()
        repository = AccountRepository(accountDao, accountsService)
    }

    fun deleteAccountById(accountId: Long): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        var isItDeleted = 0
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            isItDeleted = repository.deleteAccountById(accountId)
        }.invokeOnCompletion {
            when (isItDeleted) {
                HttpConstants.FAILED -> {
                    isDeleted.postValue(false)
                    DeleteAccountWorker.initPeriodicWorker(accountId, getApplication())
                }
                HttpConstants.UNAUTHORISED -> {
                    isDeleted.postValue(false)
                }
                HttpConstants.NO_CONTENT_SUCCESS -> {
                    isDeleted.postValue(true)
                }
            }
            isLoading.postValue(false)

        }
        return isDeleted
    }


}