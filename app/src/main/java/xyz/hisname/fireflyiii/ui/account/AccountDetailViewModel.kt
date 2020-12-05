package xyz.hisname.fireflyiii.ui.account

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.repository.account.TransactionPageSource
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.Sixple
import java.math.BigDecimal
import java.math.RoundingMode

class AccountDetailViewModel(application: Application): BaseViewModel(application) {

    private val accountRepository = AccountRepository(
            AppDatabase.getInstance(application).accountDataDao(),
            genericService()?.create(AccountsService::class.java)
    )
    private val transactionDao = AppDatabase.getInstance(application).transactionDataDao()

    private val transactionRepository = TransactionRepository(
            transactionDao, genericService()?.create(TransactionService::class.java)
    )

    var currencySymbol = ""
        private set

    var accountName = ""
        private set

    val lineChartData = MutableLiveData<Sixple<BigDecimal,BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal>>()
    val uniqueExpensesCategoryLiveData = MutableLiveData<List<Triple<Float, String, BigDecimal>>>()
    val uniqueIncomeCategoryLiveData = MutableLiveData<List<Triple<Float, String, BigDecimal>>>()
    val uniqueBudgetLiveData = MutableLiveData<List<Triple<Float, String, BigDecimal>>>()

    fun getAccountById(accountId: Long): MutableLiveData<MutableList<AccountData>>{
        val accountDataLiveData = MutableLiveData<MutableList<AccountData>>()
        viewModelScope.launch(Dispatchers.IO){
            val accountList = accountRepository.getAccountById(accountId)
            currencySymbol = accountList[0].accountAttributes?.currency_symbol ?: ""
            accountName =  accountList[0].accountAttributes?.name ?: ""
            getTransactions(accountId)
            accountDataLiveData.postValue(accountList)
        }
        return accountDataLiveData
    }

    private suspend fun getTransactions(accountId: Long){
        accountRepository.getTransactionByAccountId(accountId, DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), "all", transactionDao)
        val startOfMonth = transactionRepository.getTransactionsByAccountAndDate(
                DateTimeUtil.getStartOfMonth(), DateTimeUtil.getStartOfMonth(), accountId)
        val weekOne = transactionRepository.getTransactionsByAccountAndDate(
                DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 1),
                accountId)
        val weekTwo = transactionRepository.getTransactionsByAccountAndDate(
                DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 2),
                accountId)
        val weekThree = transactionRepository.getTransactionsByAccountAndDate(
                DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 3),
                accountId)
        val weekFour = transactionRepository.getTransactionsByAccountAndDate(
                DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 4),
                accountId)
        val lastDayOfMonth = transactionRepository.getTransactionsByAccountAndDate(
                DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(), accountId)
        lineChartData.postValue(Sixple(startOfMonth, weekOne, weekTwo, weekThree, weekFour, lastDayOfMonth))

        getCategoryExpenses(accountId)
        getBudgetExpenses(accountId)
        getIncomeCategory(accountId)
    }

    private suspend fun getCategoryExpenses(accountId: Long) {
        val uniqueCategory = transactionRepository.getUniqueCategoryByAccountAndDateAndTypeAndCurrencyCode(accountId,
                    DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(), "withdrawal")
        var totalSumOfCategory = BigDecimal.ZERO
        val pieChartData = arrayListOf<Triple<Float, String, BigDecimal>>()
        uniqueCategory.forEach {  categorySum ->
            totalSumOfCategory += categorySum.objectSum
        }
        uniqueCategory.forEach { categorySum ->
            val percentage = categorySum.objectSum
                    .divide(totalSumOfCategory, 4, RoundingMode.HALF_UP)
                    .times(100.toBigDecimal())
                    .toFloat()
            val name = if(categorySum.objectName.isBlank()){
                getApplication<Application>().getString(R.string.expenses_without_category)
            } else {
                categorySum.objectName
            }
            pieChartData.add(Triple(percentage, name, categorySum.objectSum))
        }
        uniqueExpensesCategoryLiveData.postValue(pieChartData)
    }

    private suspend fun getBudgetExpenses(accountId: Long){
        val uniqueBudget = transactionRepository.getUniqueBudgetByAccountAndDateAndType(accountId,
                DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(), "withdrawal")
        var totalSumOfBudget = BigDecimal.ZERO
        val pieChartData = arrayListOf<Triple<Float, String, BigDecimal>>()
        uniqueBudget.forEach {  budgetSum ->
            totalSumOfBudget += budgetSum.objectSum
        }
        uniqueBudget.forEach { budgetSum ->
            val percentage = budgetSum.objectSum
                    .divide(totalSumOfBudget, 4, RoundingMode.HALF_UP)
                    .times(100.toBigDecimal())
                    .toFloat()
            val name = if(budgetSum.objectName.isBlank()){
                getApplication<Application>().getString(R.string.expenses_without_budget)
            } else {
                budgetSum.objectName
            }
            pieChartData.add(Triple(percentage, name, budgetSum.objectSum))
        }
        uniqueBudgetLiveData.postValue(pieChartData)
    }

    private suspend fun getIncomeCategory(accountId: Long){
        val uniqueCategory = transactionRepository.getUniqueCategoryByAccountAndDateAndTypeAndCurrencyCode(accountId,
                DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(), "deposit")
        var totalSumOfCategory = BigDecimal.ZERO
        val pieChartData = arrayListOf<Triple<Float, String, BigDecimal>>()
        uniqueCategory.forEach {  categorySum ->
            totalSumOfCategory += categorySum.objectSum
        }
        uniqueCategory.forEach { categorySum ->
            val percentage = categorySum.objectSum
                    .divide(totalSumOfCategory, 4, RoundingMode.HALF_UP)
                    .times(100.toBigDecimal())
                    .toFloat()
            val name = if(categorySum.objectName.isBlank()){
                getApplication<Application>().getString(R.string.income_without_category)
            } else {
                categorySum.objectName
            }
            pieChartData.add(Triple(percentage, name, categorySum.objectSum))
        }
        uniqueIncomeCategoryLiveData.postValue(pieChartData)
    }

    fun getTransactionList(accountId: Long) = Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
        TransactionPageSource(transactionDao, accountId, DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth())
    }.flow.cachedIn(viewModelScope).asLiveData()
}