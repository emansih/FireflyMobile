/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.ui.account

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.data.remote.firefly.api.AttachmentService
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.repository.attachment.AttachableType
import xyz.hisname.fireflyiii.repository.attachment.AttachmentRepository
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.workers.AttachmentWorker
import xyz.hisname.fireflyiii.workers.account.AccountWorker
import java.io.File
import java.net.UnknownHostException

class AddAccountViewModel(application: Application): BaseViewModel(application) {

    private val currencyRepository = CurrencyRepository(
            AppDatabase.getInstance(application, getUniqueHash()).currencyDataDao(),
            genericService().create(CurrencyService::class.java)
    )
    private val accountRepository = AccountRepository(
            AppDatabase.getInstance(application, getUniqueHash()).accountDataDao(),
            genericService().create(AccountsService::class.java)
    )

    private val attachmentDao = AppDatabase.getInstance(getApplication(), getUniqueHash()).attachmentDataDao()
    private val attachmentService = genericService().create(AttachmentService::class.java)
    private val attachmentRepository = AttachmentRepository(attachmentDao, attachmentService)


    var currency = ""
    val accountAttachment = MutableLiveData<List<AttachmentData>>()

    fun getDefaultCurrency(): LiveData<CurrencyData>{
        val currencyListLiveData = MutableLiveData<CurrencyData>()
        viewModelScope.launch(Dispatchers.IO){
            val currencyList = currencyRepository.defaultCurrency()
            currencyListLiveData.postValue(currencyList)
            currency = currencyList.currencyAttributes.code
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

    fun uploadFile(accountId: Long, fileToUpload: ArrayList<Uri>): LiveData<List<WorkInfo>> {
        return AttachmentWorker.initWorker(fileToUpload, accountId,
                getApplication<Application>(), AttachableType.Account, getUniqueHash())
    }

        fun addAccount(accountName: String, accountType: String,
                   currencyCode: String?, iban: String?, bic: String?, accountNumber: String?,
                   openingBalance: String?, openingBalanceDate: String?, accountRole: String?,
                   virtualBalance: String?, includeInNetWorth: Boolean, notes: String?, liabilityType: String?,
                   liabilityAmount: String?, liabilityStartDate: String?,
                   interest: String?, interestPeriod: String?, fileToUpload: ArrayList<Uri>): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(CoroutineExceptionHandler { _, _ -> }){
            val addAccount = accountRepository.addAccount( accountName, accountType, currencyCode,
                    iban, bic, accountNumber, openingBalance, openingBalanceDate,
                    accountRole, virtualBalance, includeInNetWorth, notes, liabilityType, liabilityAmount,
                    liabilityStartDate, interest, interestPeriod)
            when {
                addAccount.response != null -> {
                    apiResponse.postValue(Pair(true, "Account saved"))
                    if(fileToUpload.isNotEmpty()){
                        uploadFile(addAccount.response.data.accountId, fileToUpload)
                    }
                }
                addAccount.error != null -> {
                    if(addAccount.error is UnknownHostException){
                        apiResponse.postValue(Pair(true,
                                getApplication<Application>().getString(R.string.data_added_when_user_online,
                                        "Account")))
                        AccountWorker.initWorker(getApplication(), accountName, accountType, currencyCode,
                                iban, bic, accountNumber, openingBalance, openingBalanceDate,
                                accountRole, virtualBalance, includeInNetWorth, notes, liabilityType, liabilityAmount,
                                liabilityStartDate, interest, interestPeriod, fileToUpload, getUniqueHash())
                    } else {
                        apiResponse.postValue(Pair(false, addAccount.error.localizedMessage))
                    }
                }
                addAccount.errorMessage != null -> {
                    apiResponse.postValue(Pair(false,addAccount.errorMessage))
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
            accountAttachment.postValue(accountRepository.getAttachment(accountId, attachmentDao))
        }
        return accountListLiveData
    }

    fun deleteAttachment(data: AttachmentData): LiveData<Boolean>{
        val isSuccessful = MutableLiveData<Boolean>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            isLoading.postValue(false)
            val fileName = getApplication<Application>().getExternalFilesDir(null).toString() +
                    File.separator + data.attachmentAttributes.filename
            attachmentRepository.deleteAttachment(data, fileName)
        }
        return isSuccessful
    }
}