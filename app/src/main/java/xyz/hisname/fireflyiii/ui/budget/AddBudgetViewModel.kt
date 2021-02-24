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

package xyz.hisname.fireflyiii.ui.budget

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.BudgetService
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.data.remote.firefly.api.SystemInfoService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.attachment.AttachableType
import xyz.hisname.fireflyiii.repository.budget.BudgetRepository
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData
import xyz.hisname.fireflyiii.repository.budget.BudgetType
import xyz.hisname.fireflyiii.repository.models.budget.limits.BudgetLimitData
import xyz.hisname.fireflyiii.repository.userinfo.SystemInfoRepository
import xyz.hisname.fireflyiii.util.Version
import xyz.hisname.fireflyiii.workers.AttachmentWorker

class AddBudgetViewModel(application: Application): BaseViewModel(application) {

    private val currencyRepository = CurrencyRepository(
            AppDatabase.getInstance(application).currencyDataDao(),
            genericService().create(CurrencyService::class.java)
    )

    private val spentDao = AppDatabase.getInstance(application).spentDataDao()
    private val budgetLimitDao = AppDatabase.getInstance(application).budgetLimitDao()
    private val budgetDao = AppDatabase.getInstance(application).budgetDataDao()
    private val budgetListDao = AppDatabase.getInstance(application).budgetListDataDao()
    private val budgetService = genericService().create(BudgetService::class.java)
    private val budgetRepository = BudgetRepository(budgetDao, budgetListDao, spentDao, budgetLimitDao, budgetService)
    val unSupportedVersion: MutableLiveData<Boolean> = MutableLiveData()

    var currency = ""
    val budgetLimitAttributesLiveData = MutableLiveData<BudgetLimitData>()

    init {
        checkVersion()
    }

    fun getDefaultCurrency(): LiveData<String>{
        val currencyToDisplay = MutableLiveData<String>()
        viewModelScope.launch(Dispatchers.IO){
            val defaultCurrency = currencyRepository.defaultCurrency()
            currency = defaultCurrency.currencyAttributes.code
            currencyToDisplay.postValue(defaultCurrency.currencyAttributes.name + " (" +
                    defaultCurrency.currencyAttributes.code + ")")
        }
        return currencyToDisplay
    }


    fun addBudget(budgetName: String, budgetType: BudgetType, currencyCode: String?,
                  budgetAmount: String?, budgetPeriod: String?, fileToUpload: ArrayList<Uri>): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, _ -> }){
            val addBudget = budgetRepository.addBudget(budgetName, budgetType, currencyCode,
                    budgetAmount, budgetPeriod)
            when {
                addBudget.response != null -> {
                    apiResponse.postValue(Pair(true, "Stored new budget"))
                    if(fileToUpload.isNotEmpty()) {
                        AttachmentWorker.initWorker(fileToUpload,
                                addBudget.response.data.budgetListId, getApplication<Application>(), AttachableType.BUDGET)
                    }
                }
                addBudget.errorMessage != null -> {
                    apiResponse.postValue(Pair(false,addBudget.errorMessage))
                }
                addBudget.error != null -> {
                    apiResponse.postValue(Pair(false, addBudget.error.localizedMessage))
                }
                else -> {
                    apiResponse.postValue(Pair(false, "Error saving budget"))
                }
            }
            isLoading.postValue(false)
        }
        return apiResponse
    }

    fun updateBudget(budgetId: Long, budgetName: String, budgetType: BudgetType, currencyCode: String?,
                  budgetAmount: String?, budgetPeriod: String?): LiveData<Pair<Boolean,String>>{
        val apiResponse = MutableLiveData<Pair<Boolean,String>>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, _ -> }){
            val addBudget = budgetRepository.updateBudget(budgetId, budgetName, budgetType, currencyCode,
                    budgetAmount, budgetPeriod)
            when {
                addBudget.response != null -> {
                    apiResponse.postValue(Pair(true, "Updated $budgetName"))
                }
                addBudget.errorMessage != null -> {
                    apiResponse.postValue(Pair(false,addBudget.errorMessage))
                }
                addBudget.error != null -> {
                    apiResponse.postValue(Pair(false, addBudget.error.localizedMessage))
                }
                else -> {
                    apiResponse.postValue(Pair(false, "Error saving budget"))
                }
            }
            isLoading.postValue(false)
        }
        return apiResponse
    }

    fun doNotShowAgain(status: Boolean){
        AppPref(sharedPref).budgetIssue4394 = status
    }

    fun getBudgetById(budgetId: Long, currencySymbol: String): LiveData<BudgetListData>{
        val budgetAttributesLiveData = MutableLiveData<BudgetListData>()
        viewModelScope.launch(Dispatchers.IO){
            val budgetLimitAttributes = budgetRepository.getBudgetLimitById(budgetId, currencySymbol)
            budgetLimitAttributesLiveData.postValue(budgetLimitAttributes)
            val budgetAttributes = budgetRepository.getBudgetListIdById(budgetId)
            budgetAttributesLiveData.postValue(budgetAttributes)
        }
        return budgetAttributesLiveData
    }

    private fun checkVersion(){
        if(!AppPref(sharedPref).budgetIssue4394){
            val systemInfoRepository = SystemInfoRepository(
                    genericService().create(SystemInfoService::class.java),
                    sharedPref,
                    accManager)
            viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { coroutineContext, throwable ->  }){
                systemInfoRepository.getUserSystem()
            }
            val fireflyVersion = AppPref(sharedPref).serverVersion
            if(fireflyVersion.contentEquals("5.5.0-beta.1")){
                unSupportedVersion.postValue(true)
            } else {
                if(Version(fireflyVersion) == Version("5.4.6") ||
                        Version(fireflyVersion).compareTo(Version("5.5.0")) == -1){
                    unSupportedVersion.postValue(true)
                }
            }
        }
    }
}