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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.BudgetService
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.budget.BudgetRepository
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.repository.models.budget.ChildIndividualBudget
import xyz.hisname.fireflyiii.repository.models.budget.IndividualBudget
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.network.HttpConstants
import java.math.BigDecimal
import kotlin.math.abs

class BudgetListViewModel(application: Application): BaseViewModel(application) {

    private lateinit var startOfMonth: String
    private lateinit var endOfMonth: String


    private val budgetRepository = BudgetRepository(
            AppDatabase.getInstance(application).budgetDataDao(),
            AppDatabase.getInstance(application).budgetListDataDao(),
            AppDatabase.getInstance(application).spentDataDao(),
            AppDatabase.getInstance(application).budgetLimitDao(),
            genericService().create(BudgetService::class.java)
    )
    private val currencyRepository = CurrencyRepository(
            AppDatabase.getInstance(application).currencyDataDao(),
            genericService().create(CurrencyService::class.java)
    )
    private var monthCount: Long = 0
    private var currencySymbol = ""

    val spentValue: MutableLiveData<String> = MutableLiveData()
    val budgetValue: MutableLiveData<String> = MutableLiveData()
    val budgetPercentage: MutableLiveData<BigDecimal> = MutableLiveData()
    val currencyName: MutableLiveData<String> = MutableLiveData()
    val individualBudget: MutableLiveData<List<IndividualBudget>> = MutableLiveData()
    val displayMonth: MutableLiveData<String> = MutableLiveData()

    init {
        setDisplayDate()
    }

    fun setDisplayDate(){
        isLoading.postValue(true)
        when {
            monthCount == 0L -> {
                // 0 -> current month
                displayMonth.postValue(DateTimeUtil.getMonthAndYear(DateTimeUtil.getTodayDate()))
                startOfMonth = DateTimeUtil.getStartOfMonth()
                endOfMonth = DateTimeUtil.getEndOfMonth()
            }
            monthCount < 1L -> {
                // Negative months will be previous months
                // -1 -> 1 month before this month
                // -2 -> 2 month before this month
                displayMonth.postValue(DateTimeUtil.getMonthAndYear(DateTimeUtil.getStartOfMonth(abs(monthCount))))
                startOfMonth = DateTimeUtil.getStartOfMonth(abs(monthCount))
                endOfMonth = DateTimeUtil.getEndOfMonth(abs(monthCount))
            }
            else -> {
                // +1 -> 1 month after this month
                // +2 -> 2 month after this month
                displayMonth.postValue(DateTimeUtil.getMonthAndYear(DateTimeUtil.getFutureStartOfMonth(monthCount)))
                startOfMonth = DateTimeUtil.getFutureStartOfMonth(monthCount)
                endOfMonth = DateTimeUtil.getFutureEndOfMonth(monthCount)
            }
        }
        viewModelScope.launch(Dispatchers.IO){
            val defaultCurrency = currencyRepository.defaultCurrency()
            currencySymbol = defaultCurrency.currencyAttributes.symbol
            currencyName.postValue(defaultCurrency.currencyAttributes.code)
            getBudgetData(defaultCurrency.currencyAttributes.code, currencySymbol, startOfMonth, endOfMonth)
            getRecyclerviewData(startOfMonth, endOfMonth)
        }
    }

    private suspend fun getBudgetData(currencyCode: String, currencySymbol: String,
                                      startOfMonth: String, endOfMonth: String){
        val budgetSpent = budgetRepository.allActiveSpentList(currencyCode,
                startOfMonth, endOfMonth)
        spentValue.postValue(currencySymbol + budgetSpent.toPlainString())
        val budgeted = budgetRepository.getConstraintBudgetWithCurrency(
                startOfMonth, endOfMonth, currencyCode)
        budgetValue.postValue(currencySymbol + budgeted.toPlainString())
        if(budgetSpent == BigDecimal.ZERO){
            budgetPercentage.postValue(BigDecimal.ZERO)
        } else {
            if (budgeted == BigDecimal.ZERO){
                budgetPercentage.postValue(BigDecimal.ZERO)
            } else {
                budgetPercentage.postValue(
                        budgetSpent.divide(budgeted, BigDecimal.ROUND_HALF_EVEN)
                                .multiply(100.toBigDecimal())
                                .setScale(2, BigDecimal.ROUND_HALF_DOWN))
            }
        }
    }

    private suspend fun getRecyclerviewData(startOfMonth: String, endOfMonth: String){
        val individualBudgetList = arrayListOf<IndividualBudget>()
        individualBudgetList.clear()
        individualBudget.postValue(individualBudgetList)
        budgetRepository.getAllBudgetList(startOfMonth, endOfMonth).forEach { uniqueBudget ->
            val budgetName = uniqueBudget.budgetListAttributes.name
            val budgetId = uniqueBudget.budgetListId
            budgetRepository.getBudgetLimit(budgetId, startOfMonth, endOfMonth)
            val uniqueSymbolList = budgetRepository.getUniqueCurrencySymbolInSpentByBudgetId(budgetId)
            val childBudgetList = arrayListOf<ChildIndividualBudget>()
            uniqueSymbolList.forEach { uniqueSymbol ->
                if (budgetName.isNotBlank()) {
                    val spentAmountByName = budgetRepository.getSpentByBudgetName(budgetName, uniqueSymbol)
                    val spentAmount = spentAmountByName ?: 0.toBigDecimal()
                    val budgetAmount = budgetRepository.getBudgetLimitByName(budgetName, startOfMonth,
                            endOfMonth, uniqueSymbol)
                    // No! It's not always false
                    val userBudgetAmount = if (budgetAmount == null) {
                        0.toBigDecimal()
                    } else {
                        budgetAmount
                    }
                    val budgetListId = budgetRepository.getBudgetListIdByName(budgetName)
                    childBudgetList.add(ChildIndividualBudget(budgetListId, spentAmount, userBudgetAmount, uniqueSymbol))
                }
            }
            if(childBudgetList.isNotEmpty()){
                individualBudgetList.add(IndividualBudget(budgetName, childBudgetList))
            }
        }
        individualBudget.postValue(individualBudgetList)
        isLoading.postValue(false)
    }

    fun minusMonth(){
        monthCount-=1
        individualBudget.postValue(arrayListOf())
        spentValue.postValue("")
        budgetValue.postValue("")
        budgetPercentage.postValue(BigDecimal.ZERO)
        setDisplayDate()
    }

    fun addMonth(){
        monthCount +=1
        individualBudget.postValue(arrayListOf())
        spentValue.postValue("")
        budgetValue.postValue("")
        budgetPercentage.postValue(BigDecimal.ZERO)
        setDisplayDate()
    }

    fun setBudget(amount: String, currencyCode: String){
        viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { coroutineContext, throwable ->
            apiResponse.postValue(throwable.localizedMessage)
        }){
            if(amount.isBlank()){
                apiResponse.postValue("Please enter some values!")
            } else {
                val budget = budgetRepository.getBudgetByCurrencyAndStartEndDate(startOfMonth, endOfMonth, currencyCode)
                val budgetData = budgetRepository.updateBudget(budget.budgetId, currencyCode, amount, startOfMonth, endOfMonth)
                budgetValue.postValue(currencySymbol + budgetData.budgetAttributes.amount.toPlainString())
            }
        }

    }

    fun deleteBudget(name: String): LiveData<Boolean> {
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            val budgetAttributes = budgetRepository.getBudgetByName(name)
            if (budgetAttributes.isNotEmpty() && budgetAttributes[0].budgetListId != 0L) {
                when (budgetRepository.deleteBudgetByName(budgetAttributes[0].budgetListId)) {
                    HttpConstants.FAILED -> {
                        isDeleted.postValue(false)
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