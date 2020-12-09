package xyz.hisname.fireflyiii.ui.account

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.workers.account.AccountWorker

class AddAccountViewModel(application: Application): BaseViewModel(application) {

    private val currencyRepository = CurrencyRepository(
            AppDatabase.getInstance(application).currencyDataDao(),
            genericService().create(CurrencyService::class.java)
    )
    private val accountRepository = AccountRepository(
            AppDatabase.getInstance(application).accountDataDao(),
            genericService().create(AccountsService::class.java)
    )

    var currency = ""

    fun getDefaultCurrency(): LiveData<CurrencyData>{
        val currencyListLiveData = MutableLiveData<CurrencyData>()
        viewModelScope.launch(Dispatchers.IO){
            val currencyList = currencyRepository.defaultCurrency()
            currencyListLiveData.postValue(currencyList)
            currency = currencyList.currencyAttributes?.code ?: ""
        }
        return currencyListLiveData
    }

    fun updateAccount(accountId: Long, accountName: String, accountType: String,
                      currencyCode: String?, iban: String?, bic: String?, accountNumber: String?,
                      openingBalance: String?, openingBalanceDate: String?, accountRole: String?,
                      virtualBalance: String?, includeInNetWorth: Boolean, notes: String?, liabilityType: String?,
                      liabilityAmount: String?, liabilityStartDate: String?, interest: String?, interestPeriod: String?): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            val addAccount = accountRepository.updateAccount(accountId, accountName, accountType, currencyCode,
                    iban, bic, accountNumber, openingBalance, openingBalanceDate,
                    accountRole, virtualBalance, includeInNetWorth, notes, liabilityType, liabilityAmount,
                    liabilityStartDate, interest, interestPeriod)
            when {
                addAccount.response != null -> {
                    apiResponse.postValue(Pair(true, "Account updated"))
                }
                addAccount.errorMessage != null -> {
                    apiResponse.postValue(Pair(false,addAccount.errorMessage))
                }
                addAccount.error != null -> {
                    apiResponse.postValue(Pair(false,addAccount.error.localizedMessage))
                }
                else -> {
                    apiResponse.postValue(Pair(false, "Error saving account"))
                }
            }
            isLoading.postValue(false)
        }
        return apiResponse
    }

    fun addAccount(accountName: String, accountType: String,
                   currencyCode: String?, iban: String?, bic: String?, accountNumber: String?,
                   openingBalance: String?, openingBalanceDate: String?, accountRole: String?,
                   virtualBalance: String?, includeInNetWorth: Boolean, notes: String?, liabilityType: String?,
                   liabilityAmount: String?, liabilityStartDate: String?, interest: String?, interestPeriod: String?): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            val addAccount = accountRepository.addAccount( accountName, accountType, currencyCode,
                    iban, bic, accountNumber, openingBalance, openingBalanceDate,
                    accountRole, virtualBalance, includeInNetWorth, notes, liabilityType, liabilityAmount,
                    liabilityStartDate, interest, interestPeriod)
            when {
                addAccount.response != null -> {
                    apiResponse.postValue(Pair(true, "Account saved"))
                }
                addAccount.errorMessage != null -> {
                    apiResponse.postValue(Pair(false,addAccount.errorMessage))
                }
                addAccount.error != null -> {
                    apiResponse.postValue(Pair(false,addAccount.error.localizedMessage))
                    AccountWorker.initWorker(getApplication(), accountName, accountType, currencyCode,
                            iban, bic, accountNumber, openingBalance, openingBalanceDate,
                            accountRole, virtualBalance, includeInNetWorth, notes, liabilityType, liabilityAmount,
                            liabilityStartDate, interest, interestPeriod)
                }
                else -> {
                    apiResponse.postValue(Pair(false, "Error saving account"))
                }
            }
            isLoading.postValue(false)
        }
        return apiResponse
    }

    fun getAccountById(accountId: Long): LiveData<AccountData>{
        val accountListLiveData = MutableLiveData<AccountData>()
        viewModelScope.launch(Dispatchers.IO){
            accountListLiveData.postValue(accountRepository.getAccountById(accountId))
        }
        return accountListLiveData
    }
}