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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.BudgetService
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.budget.BudgetRepository
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.repository.models.budget.IndividualBudget
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.DateTimeUtil
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
    private val transactionRepository = TransactionRepository(
            AppDatabase.getInstance(application).transactionDataDao(),
            genericService().create(TransactionService::class.java)
    )
    private var monthCount: Long = 0

    val spentValue: MutableLiveData<String> = MutableLiveData()
    val budgetValue: MutableLiveData<String> = MutableLiveData()
    val budgetPercentage: MutableLiveData<BigDecimal> = MutableLiveData()
    val currencyName: MutableLiveData<String> = MutableLiveData()
    val individualBudget: MutableLiveData<List<IndividualBudget>> = MutableLiveData()
    val displayMonth: MutableLiveData<String> = MutableLiveData()

    init {
        setDisplayDate()
    }

    private fun setDisplayDate(){
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
            currencyName.postValue(defaultCurrency.currencyAttributes.code)
            transactionRepository.getTransactionByDateAndCurrencyCode(startOfMonth, endOfMonth,
                    defaultCurrency.currencyAttributes.code, "withdrawal", true)
            getBudgetData(defaultCurrency.currencyAttributes.code,
                    defaultCurrency.currencyAttributes.symbol, startOfMonth, endOfMonth)
            getRecyclerviewData(defaultCurrency.currencyAttributes.code,
                    defaultCurrency.currencyAttributes.symbol, startOfMonth, endOfMonth)
        }
    }

    private suspend fun getBudgetData(currencyCode: String, currencySymbol: String,
                                      startOfMonth: String, endOfMonth: String){
        val budgetSpent = budgetRepository.allActiveSpentList(currencyCode,
                startOfMonth, endOfMonth)
        spentValue.postValue(currencySymbol + budgetSpent)
        budgetRepository.getAllBudget()
        val budgeted = budgetRepository.retrieveConstraintBudgetWithCurrency(
                startOfMonth, endOfMonth, currencyCode)
        budgetValue.postValue(currencySymbol + budgeted)
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

    private suspend fun getRecyclerviewData(currencyCode: String, currencySymbol: String,
                                            startOfMonth: String, endOfMonth: String){
        val uniqueBudget = transactionRepository.getUniqueBudgetByDate(startOfMonth, endOfMonth,
                currencyCode, "withdrawal")
        val individualBudgetList = arrayListOf<IndividualBudget>()
        uniqueBudget.forEach { budget ->
            if(budget.isNotBlank()){
                val spentAmount = transactionRepository.getTransactionByDateAndBudgetAndCurrency(startOfMonth, endOfMonth,
                        currencyCode, "withdrawal", budget)
                val budgetAmount = budgetRepository.getBudgetLimitByName(budget, startOfMonth,
                        endOfMonth, currencyCode)
                individualBudgetList.add(IndividualBudget(budget, spentAmount, budgetAmount, currencySymbol))
            }
        }
        individualBudget.postValue(individualBudgetList)
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
}