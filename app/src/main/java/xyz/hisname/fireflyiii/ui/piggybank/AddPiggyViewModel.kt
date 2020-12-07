package xyz.hisname.fireflyiii.ui.piggybank

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.piggybank.PiggyRepository
import xyz.hisname.fireflyiii.workers.piggybank.PiggyBankWorker
import java.net.UnknownHostException

class AddPiggyViewModel(application: Application): BaseViewModel(application) {

    private val piggyRepository = PiggyRepository(
            AppDatabase.getInstance(application).piggyDataDao(),
            genericService()?.create(PiggybankService::class.java)
    )

    private val accountRepository = AccountRepository(
            AppDatabase.getInstance(application).accountDataDao(),
            genericService()?.create(AccountsService::class.java)
    )

    fun getPiggyById(piggyId: Long): LiveData<PiggyData>{
        val piggyListLiveData = MutableLiveData<PiggyData>()
        viewModelScope.launch(Dispatchers.IO){
            val piggyList = piggyRepository.getPiggyById(piggyId)
            accountRepository.getAccountById(piggyList.piggyAttributes?.account_id ?: 0)
            piggyList.piggyAttributes?.account_id
            piggyListLiveData.postValue(piggyList)
        }
        return piggyListLiveData
    }

    fun getAccount(): LiveData<List<String>>{
        val accountListLiveData = MutableLiveData<List<String>>()
        viewModelScope.launch(Dispatchers.IO){
            val account = arrayListOf<String>()
            accountRepository.getAccountByType("asset").collectLatest { accountDataList ->
                accountDataList.forEach { accountData ->
                    accountData.accountAttributes?.name?.let { account.add(it) }
                }
            }
        }
        return accountListLiveData
    }

    fun addPiggyBank(piggyName: String, accountName: String, currentAmount: String?, notes: String?,
                     startDate: String?, targetAmount: String, targetDate: String?): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            val accountId = accountRepository.getAccountByName(accountName, "asset").accountId
            if(accountId != null || accountId == 0L){
                val addPiggyBank = piggyRepository.addPiggyBank(piggyName, accountId, targetAmount,
                        currentAmount, startDate, targetDate, notes)
                when {
                    addPiggyBank.response != null -> {
                        apiResponse.postValue(Pair(true, "Piggy bank saved"))
                    }
                    addPiggyBank.errorMessage != null -> {
                        apiResponse.postValue(Pair(false, addPiggyBank.errorMessage))
                    }
                    addPiggyBank.error != null -> {
                        if(addPiggyBank.error is UnknownHostException){
                            PiggyBankWorker.initWorker(getApplication(), piggyName, accountId.toString(), targetAmount,
                                    currentAmount, startDate, targetDate, notes)
                            apiResponse.postValue(Pair(false, getApplication<Application>().getString(R.string.data_added_when_user_online, "Piggy Bank")))
                        } else {
                            apiResponse.postValue(Pair(false, addPiggyBank.error.localizedMessage))
                        }

                    }
                    else -> {
                        apiResponse.postValue(Pair(false, "Error saving piggy bank"))
                    }
                }
                isLoading.postValue(false)
            } else {
                apiResponse.postValue(Pair(false, "There was an error getting account data"))
                isLoading.postValue(false)
            }
        }
        return apiResponse
    }

    fun updatePiggyBank(piggyId: Long, piggyName: String, accountName: String, currentAmount: String?, notes: String?,
                        startDate: String?, targetAmount: String, targetDate: String?): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val originalAccountName = piggyRepository.getPiggyById(piggyId).piggyAttributes?.account_name ?: ""
            val accountIdFromRepo = accountRepository.getAccountByName(accountName, "asset").accountId

            val accountId = if(accountName.contentEquals(originalAccountName)){
                piggyRepository.getPiggyById(piggyId).piggyAttributes?.account_id
            } else {
                accountIdFromRepo
            }
            if(accountId != null && accountId != 0L){
                val updatePiggyBank = piggyRepository.updatePiggyBank(piggyId, piggyName, accountId, targetAmount,
                        currentAmount, startDate, targetDate, notes)
                when {
                    updatePiggyBank.response != null -> {
                        apiResponse.postValue(Pair(true, "Piggy bank updated"))
                    }
                    updatePiggyBank.errorMessage != null -> {
                        apiResponse.postValue(Pair(false, updatePiggyBank.errorMessage))
                    }
                    updatePiggyBank.error != null -> {
                        apiResponse.postValue(Pair(false, updatePiggyBank.error.localizedMessage))
                    }
                    else -> {
                        apiResponse.postValue(Pair(false, "Error updating piggy bank"))
                    }
                }
            } else {
                apiResponse.postValue(Pair(false, "There was an error getting account data"))
                isLoading.postValue(false)
            }
        }
        return apiResponse
    }
}