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

package xyz.hisname.fireflyiii.ui.piggybank

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.data.remote.firefly.api.AttachmentService
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.data.remote.firefly.api.SystemInfoService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.repository.attachment.AttachableType
import xyz.hisname.fireflyiii.repository.attachment.AttachmentRepository
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.piggybank.PiggyRepository
import xyz.hisname.fireflyiii.repository.userinfo.SystemInfoRepository
import xyz.hisname.fireflyiii.util.Version
import xyz.hisname.fireflyiii.workers.AttachmentWorker
import xyz.hisname.fireflyiii.workers.piggybank.PiggyBankWorker
import java.io.File
import java.net.UnknownHostException

class AddPiggyViewModel(application: Application): BaseViewModel(application) {

    private val piggyRepository = PiggyRepository(
            AppDatabase.getInstance(application, getUniqueHash()).piggyDataDao(),
            genericService().create(PiggybankService::class.java)
    )

    private val accountRepository = AccountRepository(
            AppDatabase.getInstance(application, getUniqueHash()).accountDataDao(),
            genericService().create(AccountsService::class.java)
    )
    private val attachmentDao = AppDatabase.getInstance(getApplication(), getUniqueHash()).attachmentDataDao()
    private val attachmentService = genericService().create(AttachmentService::class.java)
    private val attachmentRepository = AttachmentRepository(attachmentDao, attachmentService)
    private val shadowAccountList = arrayListOf<String>()
    private var currentSelectedAccount = ""

    val piggyAttachment = MutableLiveData<List<AttachmentData>>()
    val unSupportedVersion: MutableLiveData<Boolean> = MutableLiveData()

    init {
        checkVersion()
    }

    fun getPiggyById(piggyId: Long): LiveData<PiggyData>{
        val piggyListLiveData = MutableLiveData<PiggyData>()
        viewModelScope.launch(Dispatchers.IO){
            val piggyList = piggyRepository.getPiggyById(piggyId)
            accountRepository.getAccountById(piggyList.piggyAttributes.account_id ?: 0)
            piggyListLiveData.postValue(piggyList)
            piggyAttachment.postValue(piggyRepository.getAttachment(piggyId, attachmentDao))
            currentSelectedAccount = piggyList.piggyAttributes.account_name ?: ""
        }
        return piggyListLiveData
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

    fun getAccountById(accountId: Long): LiveData<AccountData>{
        val accountLiveData = MutableLiveData<AccountData>()
        viewModelScope.launch(Dispatchers.IO) {
            accountLiveData.postValue(accountRepository.getAccountById(accountId))
        }
        return accountLiveData
    }

    fun getAccount(): LiveData<List<String>>{
        val accountListLiveData = MutableLiveData<List<String>>()
        viewModelScope.launch(Dispatchers.IO){
            val accountList = arrayListOf<String>()
            accountRepository.getAccountByType("asset").forEach {  data ->
                accountList.add(data.accountAttributes.name + "  (" + data.accountAttributes.currency_symbol
                        + data.accountAttributes.current_balance + ")")
                shadowAccountList.add(data.accountAttributes.name)
            }
            accountListLiveData.postValue(accountList)
        }
        return accountListLiveData
    }

    fun getCurrentSelectedAccount(position: Int){
        currentSelectedAccount = shadowAccountList[position]
    }

    fun uploadFile(piggyBankId: Long, fileToUpload: ArrayList<Uri>): LiveData<List<WorkInfo>>{
        return AttachmentWorker.initWorker(fileToUpload, piggyBankId, getApplication<Application>(),
                AttachableType.PIGGYBANK)
    }

    fun addPiggyBank(piggyName: String, currentAmount: String?, notes: String?,
                     startDate: String?, targetAmount: String, targetDate: String?, group: String?,
                     fileToUpload: ArrayList<Uri>): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            apiResponse.postValue(Pair(false, "There was an error getting account data"))
            isLoading.postValue(false)
        }){
            val accountId = accountRepository.getAccountByName(currentSelectedAccount, "asset").accountId
            if(accountId != 0L) {
                val addPiggyBank = piggyRepository.addPiggyBank(piggyName, accountId, targetAmount,
                        currentAmount, startDate, targetDate, notes, group)
                when {
                    addPiggyBank.response != null -> {
                        apiResponse.postValue(Pair(true, "Piggy bank saved"))
                        if(fileToUpload.isNotEmpty()) {
                            uploadFile(addPiggyBank.response.data.piggyId, fileToUpload)
                        }
                    }
                    addPiggyBank.errorMessage != null -> {
                        apiResponse.postValue(Pair(false, addPiggyBank.errorMessage))
                    }
                    addPiggyBank.error != null -> {
                        if (addPiggyBank.error is UnknownHostException) {
                            PiggyBankWorker.initWorker(getApplication(), piggyName, accountId.toString(), targetAmount,
                                    currentAmount, startDate, targetDate, notes, group, fileToUpload)
                            apiResponse.postValue(Pair(true, getApplication<Application>().getString(R.string.data_added_when_user_online, "Piggy Bank")))
                        } else {
                            apiResponse.postValue(Pair(false, addPiggyBank.error.localizedMessage))
                        }
                    }
                    else -> {
                        apiResponse.postValue(Pair(false, "Error saving piggy bank"))
                    }
                }
            }
            isLoading.postValue(false)
        }
        return apiResponse
    }

    fun updatePiggyBank(piggyId: Long, piggyName: String, currentAmount: String?, notes: String?,
                        startDate: String?, targetAmount: String, targetDate: String?, group: String?): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val originalAccountName = piggyRepository.getPiggyById(piggyId).piggyAttributes.account_name ?: ""
            val accountIdFromRepo = accountRepository.getAccountByName(currentSelectedAccount, "asset").accountId

            val accountId = if(currentSelectedAccount.contentEquals(originalAccountName)){
                piggyRepository.getPiggyById(piggyId).piggyAttributes.account_id
            } else {
                accountIdFromRepo
            }
            if(accountId != null && accountId != 0L){
                val updatePiggyBank = piggyRepository.updatePiggyBank(piggyId, piggyName, accountId, targetAmount,
                        currentAmount, startDate, targetDate, notes, group)
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
            }
            isLoading.postValue(false)
        }
        return apiResponse
    }

    private fun checkVersion(){
        if(!AppPref(sharedPref).budgetIssue4394){
            val systemInfoRepository = SystemInfoRepository(
                    genericService().create(SystemInfoService::class.java),
                    sharedPref, newManager)
            viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { coroutineContext, throwable ->  }){
                systemInfoRepository.getUserSystem()
            }
            val fireflyVersion = AppPref(sharedPref).serverVersion
            if(fireflyVersion.contentEquals("5.5.0-beta.1")){
                unSupportedVersion.postValue(true)
            } else {
                if(Version(fireflyVersion).compareTo(Version("5.5.0")) == -1){
                    unSupportedVersion.postValue(true)
                }
            }
        }
    }
}