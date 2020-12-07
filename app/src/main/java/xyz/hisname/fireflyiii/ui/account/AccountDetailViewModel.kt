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
import xyz.hisname.fireflyiii.repository.models.DetailModel
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.DateTimeUtil
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
    private var accountType = ""
    var currencySymbol = ""
        private set

    var accountName = ""
        private set

    val uniqueExpensesCategoryLiveData = MutableLiveData<List<Triple<Float, String, BigDecimal>>>()
    val uniqueIncomeCategoryLiveData = MutableLiveData<List<Triple<Float, String, BigDecimal>>>()
    val uniqueBudgetLiveData = MutableLiveData<List<Triple<Float, String, BigDecimal>>>()
    val accountData = MutableLiveData<List<DetailModel>>()
    val notes = MutableLiveData<String>()

    fun getAccountById(accountId: Long): MutableLiveData<AccountData>{
        val accountDataLiveData = MutableLiveData<AccountData>()
        viewModelScope.launch(Dispatchers.IO){
            val accountList = accountRepository.getAccountById(accountId)
            val accountAttributes = accountList.accountAttributes
            currencySymbol = accountAttributes?.currency_symbol ?: ""
            accountName =  accountAttributes?.name ?: ""
            accountType = accountAttributes?.type ?: ""
            val arrayListOfDetails = arrayListOf(
                    DetailModel(getApplication<Application>().getString(R.string.balance),
                            currencySymbol + accountAttributes?.current_balance),
                    DetailModel(getApplication<Application>().getString(R.string.account_number),
                            accountAttributes?.account_number.toString()),
                    DetailModel("Role", accountAttributes?.account_role),
                    DetailModel("Account Type", accountType),
                    DetailModel("Active ", accountAttributes?.active.toString())
            )
            if(accountType.contentEquals("liabilities")){
                arrayListOfDetails.add(DetailModel("Type of liability", accountAttributes?.liability_type))
                arrayListOfDetails.add(DetailModel(getApplication<Application>().getString(R.string.interest),
                        accountAttributes?.interest + "% (" + accountAttributes?.interest_period + ")"))
            }
            accountData.postValue(arrayListOfDetails)
            notes.postValue(accountAttributes?.notes)
            getTransactions(accountId, accountType)
            accountDataLiveData.postValue(accountList)
        }
        return accountDataLiveData
    }

    private suspend fun getTransactions(accountId: Long, accountType: String){
        accountRepository.getTransactionByAccountId(accountId, DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), "all", transactionDao)
        getCategoryExpenses(accountId, accountType)
        getBudgetExpenses(accountId)
        getIncomeCategory(accountId, accountType)
    }

    private suspend fun getCategoryExpenses(accountId: Long, accountType: String) {
        val uniqueCategory = if(accountType.contentEquals("asset") || accountType.contentEquals("revenue")){
            transactionRepository.getUniqueCategoryBySourceAndDateAndType(accountId,
                    DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(), "withdrawal")
        } else {
            transactionRepository.getUniqueCategoryByDestinationAndDateAndType(accountId,
                    DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(), "withdrawal")
        }
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
        val uniqueBudget = if(accountType.contentEquals("asset") || accountType.contentEquals("revenue") ){
            transactionRepository.getUniqueBudgetBySourceAndDateAndType(accountId,
                    DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(), "withdrawal")
        } else {
            transactionRepository.getUniqueBudgetByDestinationAndDateAndType(accountId,
                    DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(), "withdrawal")
        }
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

    private suspend fun getIncomeCategory(accountId: Long, accountType: String){
        val uniqueCategory = if(accountType.contentEquals("asset") || accountType.contentEquals("revenue")){
            transactionRepository.getUniqueCategoryByDestinationAndDateAndType(accountId,
                    DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(), "deposit")
        } else {
            transactionRepository.getUniqueCategoryBySourceAndDateAndType(accountId,
                    DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(), "deposit")
        }
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
        TransactionPageSource(transactionDao, accountId, accountType, DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth())
    }.flow.cachedIn(viewModelScope).asLiveData()
}