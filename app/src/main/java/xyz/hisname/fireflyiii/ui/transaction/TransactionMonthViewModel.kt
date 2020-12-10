package xyz.hisname.fireflyiii.ui.transaction

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.repository.transaction.TransactionPagingSource
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.DateTimeUtil
import java.math.BigDecimal
import java.math.RoundingMode

class TransactionMonthViewModel(application: Application): BaseViewModel(application) {


    private val transactionDataDao = AppDatabase.getInstance(application).transactionDataDao()
    private val transactionService = genericService().create(TransactionService::class.java)
    private val transactionRepository = TransactionRepository(transactionDataDao, transactionService)
    private val currencyRepository = CurrencyRepository(
            AppDatabase.getInstance(application).currencyDataDao(),
            genericService().create(CurrencyService::class.java)
    )

    private var currencyCode = ""

    var currencySymbol = ""
        private set

    val totalSumLiveData = MutableLiveData<String>()

    private fun getStartOfMonth(monthYear: Int): String{
        return if(monthYear == 0){
            DateTimeUtil.getStartOfMonth()
        } else {
            DateTimeUtil.getStartOfMonth(monthYear.toLong())
        }
    }

    private fun getEndOfMonth(monthYear: Int): String{
        return if(monthYear == 0){
            DateTimeUtil.getEndOfMonth()
        } else {
            DateTimeUtil.getEndOfMonth(monthYear.toLong())
        }
    }

    private var transactionSum = 0.toBigDecimal()

    fun getCategoryData(transactionType: String, monthYear: Int): LiveData<List<Triple<Float, String, BigDecimal>>>{
        val uniqueCategoryLiveData = MutableLiveData<List<Triple<Float, String, BigDecimal>>>()
        viewModelScope.launch(Dispatchers.IO){
            val defaultCurrency = currencyRepository.defaultCurrency().currencyAttributes
            currencyCode = defaultCurrency.code
            transactionSum = transactionRepository.getTransactionByDateAndCurrencyCode(getStartOfMonth(monthYear),
                getEndOfMonth(monthYear), currencyCode, transactionType, false)
            totalSumLiveData.postValue(transactionSum.toString())
            currencySymbol = defaultCurrency.symbol
            val uniqueCategory = transactionRepository.getUniqueCategoryByDateAndType(getStartOfMonth(monthYear),
                    getEndOfMonth(monthYear), currencyCode, transactionType)
            val pieChartData = arrayListOf<Triple<Float, String, BigDecimal>>()
            if(transactionSum > BigDecimal.ZERO){
                uniqueCategory.forEach { categorySum ->
                    val percentage = categorySum.objectSum
                            .divide(transactionSum, 4, RoundingMode.HALF_UP)
                            .times(100.toBigDecimal())
                            .toFloat()
                    val name = if(categorySum.objectName.isBlank()){
                        getApplication<Application>().getString(R.string.expenses_without_category)
                    } else {
                        categorySum.objectName
                    }
                    pieChartData.add(Triple(percentage, name, categorySum.objectSum))
                }
                uniqueCategoryLiveData.postValue(pieChartData)
            }

        }
        return uniqueCategoryLiveData
    }

    fun getBudgetData(transactionType: String, monthYear: Int): LiveData<List<Triple<Float, String, BigDecimal>>>{
        val uniqueBudgetLiveData = MutableLiveData<List<Triple<Float, String, BigDecimal>>>()
        viewModelScope.launch(Dispatchers.IO) {
            val uniqueBudget = transactionRepository.getUniqueBudgetByDateAndType(getStartOfMonth(monthYear),
                    getEndOfMonth(monthYear), currencyCode, transactionType)
            val pieChartData = arrayListOf<Triple<Float, String, BigDecimal>>()
            if(transactionSum > BigDecimal.ZERO) {
                uniqueBudget.forEach { categorySum ->
                    val percentage = categorySum.objectSum
                            .divide(transactionSum, 4, RoundingMode.HALF_UP)
                            .times(100.toBigDecimal())
                            .toFloat()
                    val name = if(categorySum.objectName.isBlank()){
                        getApplication<Application>().getString(R.string.expenses_without_budget)
                    } else {
                        categorySum.objectName
                    }
                    pieChartData.add(Triple(percentage, name, categorySum.objectSum))
                }
                uniqueBudgetLiveData.postValue(pieChartData)
            }

        }
        return uniqueBudgetLiveData
    }


    fun getAccount(transactionType: String, monthYear: Int): LiveData<List<Triple<Float, String, BigDecimal>>>{
        val uniqueAccountLiveData = MutableLiveData<List<Triple<Float, String, BigDecimal>>>()
        viewModelScope.launch(Dispatchers.IO){
            val uniqueAccount = transactionRepository.getDestinationAccountByTypeAndDate(getStartOfMonth(monthYear),
                    getEndOfMonth(monthYear), currencyCode, transactionType)
            val pieChartData = arrayListOf<Triple<Float, String, BigDecimal>>()
            if(transactionSum > BigDecimal.ZERO) {
                uniqueAccount.forEach { accountSum ->
                    val percentage = accountSum.objectSum
                            .divide(transactionSum, 4, RoundingMode.HALF_UP)
                            .times(100.toBigDecimal())
                            .toFloat()
                    pieChartData.add(Triple(percentage, accountSum.objectName, accountSum.objectSum))
                }
                uniqueAccountLiveData.postValue(pieChartData)
            }
        }
        return uniqueAccountLiveData
    }

    fun getTransactionList(transactionType: String, monthYear: Int): LiveData<PagingData<Transactions>> {
        return Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)) {
            TransactionPagingSource(transactionService, transactionDataDao,
                    getStartOfMonth(monthYear), getEndOfMonth(monthYear), transactionType)
        }.flow.cachedIn(viewModelScope).asLiveData()
    }
}