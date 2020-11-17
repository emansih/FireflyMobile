package xyz.hisname.fireflyiii.ui.budget

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.BudgetService
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.budget.BudgetRepository
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.DateTimeUtil
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

class BudgetSummaryViewModel(application: Application): BaseViewModel(application) {

    private val currencyRepository = CurrencyRepository(
            AppDatabase.getInstance(application).currencyDataDao(),
            genericService()?.create(CurrencyService::class.java))

    private val transactionRepository = TransactionRepository(
            AppDatabase.getInstance(application).transactionDataDao(),
            genericService()?.create(TransactionService::class.java))

    private val spentDao = AppDatabase.getInstance(application).spentDataDao()
    private val budgetLimitDao = AppDatabase.getInstance(application).budgetLimitDao()
    private val budgetDao = AppDatabase.getInstance(application).budgetDataDao()
    private val budgetListDao = AppDatabase.getInstance(application).budgetListDataDao()
    private val budgetService = genericService()?.create(BudgetService::class.java)

    private val budgetRepository = BudgetRepository(budgetDao, budgetListDao, spentDao, budgetLimitDao, budgetService)
    private lateinit var startOfMonth: String
    private lateinit var endOfMonth: String

    private var defaultCurrency = ""
    private var originalBudget: BigDecimal = 0.toBigDecimal()
    var originalRemainderString = ""
    var originalBudgetString = ""
    var originalSpentString = ""

    private var sumOfWithdrawal: BigDecimal = 0.toBigDecimal()
    private val modifiedList = arrayListOf<String>()
    var currencySymbol = ""
    val totalTransaction: MutableLiveData<String> = MutableLiveData()
    val availableBudget: MutableLiveData<String> = MutableLiveData()
    val balanceBudget: MutableLiveData<String> = MutableLiveData()
    val uniqueBudgets: MutableLiveData<List<String>> = MutableLiveData()
    val pieChartData: MutableLiveData<List<Triple<Float, String, BigDecimal>>> = MutableLiveData()

    var monthCount: Long = 0

    fun getCurrency(): LiveData<List<String>> {
        val data: MutableLiveData<List<String>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            val currencyList = currencyRepository.getAllCurrency()
            currencyList[0].currencyAttributes?.decimal_places
            defaultCurrency = currencyList[0].currencyAttributes?.code ?: ""
            currencySymbol = currencyList[0].currencyAttributes?.symbol ?: ""
            currencyList.forEach {  currencyData ->
                modifiedList.add(currencyData.currencyAttributes?.name + " (${currencyData.currencyAttributes?.symbol})")
            }
            data.postValue(modifiedList)
            getTransaction()
        }
        return data
    }

    private suspend fun getTransaction(){
        transactionRepository.allWithdrawalWithCurrencyCode(startOfMonth, endOfMonth, defaultCurrency)
        val uniqBudget = transactionRepository.getUniqueBudgetByDate(
                DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(),
                defaultCurrency, "withdrawal")
        sumOfWithdrawal = 0.toBigDecimal()
        uniqueBudgets.postValue(uniqBudget)

        val budget = budgetRepository.getAllAvailableBudget(startOfMonth, endOfMonth, defaultCurrency)
        val returnData = arrayListOf<Triple<Float, String, BigDecimal>>()
        uniqBudget.forEach { budgetName ->
            if (budgetName.isNotEmpty()){
                val transactionBudget = retrieveBudget(startOfMonth, endOfMonth,
                        defaultCurrency, budgetName)
                sumOfWithdrawal = sumOfWithdrawal.add(transactionBudget)
                if(budget != BigDecimal.ZERO){
                    val percentage = transactionBudget
                            .divide(budget, 2, RoundingMode.HALF_UP)
                            .times(100.toBigDecimal())
                            .toFloat()
                    returnData.add(Triple(percentage, budgetName, transactionBudget))
                }
            }
        }

        val expensesWithoutBudget = budget.minus(sumOfWithdrawal)
        val percentage = if(expensesWithoutBudget != BigDecimal.ZERO && expensesWithoutBudget != BigDecimal.ZERO){
            expensesWithoutBudget
                    .divide(budget,2, RoundingMode.HALF_UP)
                    .times(100.toBigDecimal())
                    .toFloat()
        } else {
            0f
        }
        returnData.add(Triple(percentage,
                getApplication<Application>().getString(R.string.expenses_without_budget),
                expensesWithoutBudget))
        pieChartData.postValue(returnData)
        originalBudget = budget
        val remainder = budget.minus(sumOfWithdrawal)

        originalRemainderString = "$currencySymbol $remainder"
        originalBudgetString = "$currencySymbol $budget"
        originalSpentString = "$currencySymbol $sumOfWithdrawal"

        totalTransaction.postValue("$currencySymbol $sumOfWithdrawal")
        availableBudget.postValue("$currencySymbol $budget")
        balanceBudget.postValue("$currencySymbol $remainder")

    }

    private suspend fun retrieveBudget(start: String, end: String, currency: String, budgetName: String) =
        transactionRepository.getTransactionByDateAndBudgetAndCurrency(
                start, end, currency, "withdrawal", budgetName)

    fun getTransactionList(budget: String?): LiveData<List<Transactions>>{
        val data: MutableLiveData<List<Transactions>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            if(budget == null){
                data.postValue(transactionRepository.transactionListWithCurrency(startOfMonth, endOfMonth,
                        "withdrawal", defaultCurrency))
            } else {
                if(budget.isEmpty()){
                    balanceBudget.postValue("--.--")
                    availableBudget.postValue("--.--")

                    totalTransaction.postValue(currencySymbol + " " +
                            retrieveBudget(DateTimeUtil.getStartOfMonth(),
                                    DateTimeUtil.getEndOfMonth(), defaultCurrency, ""))
                    data.postValue(transactionRepository.transactionListWithCurrency(startOfMonth, endOfMonth,
                            "withdrawal", defaultCurrency))
                } else {
                    val budgetAmount = budgetRepository.getBudgetLimitByName(budget, DateTimeUtil.getStartOfMonth(),
                            DateTimeUtil.getEndOfMonth(), defaultCurrency)
                    availableBudget.postValue("$currencySymbol $budgetAmount")
                    val balance = budgetAmount.minus( retrieveBudget(DateTimeUtil.getStartOfMonth(),
                            DateTimeUtil.getEndOfMonth(), defaultCurrency, budget))
                    balanceBudget.postValue("$currencySymbol $balance")

                    data.postValue(transactionRepository.getTransactionListByDateAndBudget(DateTimeUtil.getStartOfMonth(),
                            DateTimeUtil.getEndOfMonth(), budget, defaultCurrency))
                }
            }
        }
        return data
    }

    fun changeCurrency(position: Int){
        val regex = "\\([^()]*\\)".toRegex()
        val regexReplaced = regex.find(modifiedList[position])
        val replacedCurrency = modifiedList[position].replace(regexReplaced?.value ?: "", "").trim()
        viewModelScope.launch(Dispatchers.IO){
            val currencyList = currencyRepository.getCurrencyCode(replacedCurrency)
            defaultCurrency = currencyList[0].currencyAttributes?.code ?: ""
            currencySymbol = currencyList[0].currencyAttributes?.symbol ?: ""
            getTransaction()
        }
    }

    fun getBalance(budget: String){
        viewModelScope.launch(Dispatchers.IO){
            if (budget.isNotEmpty()){
                val transactionBudget = retrieveBudget(startOfMonth, endOfMonth,
                        defaultCurrency, budget)
                totalTransaction.postValue("$currencySymbol $transactionBudget")
                balanceBudget.postValue("$currencySymbol ${originalBudget.minus(transactionBudget)}")
            }
        }
    }

    fun setDisplayDate(): LiveData<String>{
        val data: MutableLiveData<String> = MutableLiveData()
        when {
            monthCount == 0L -> {
                // 0 -> current month
                data.postValue(DateTimeUtil.getMonthAndYear(DateTimeUtil.getTodayDate()))
                startOfMonth = DateTimeUtil.getStartOfMonth()
                endOfMonth = DateTimeUtil.getEndOfMonth()
            }
            monthCount < 1L -> {
                // Negative months will be previous months
                // -1 -> 1 month before this month
                // -2 -> 2 month before this month
                data.postValue(DateTimeUtil.getMonthAndYear(DateTimeUtil.getStartOfMonth(abs(monthCount))))
                startOfMonth = DateTimeUtil.getStartOfMonth(1)
                endOfMonth = DateTimeUtil.getEndOfMonth(1)
            }
            else -> {
                // +1 -> 1 month after this month
                // +2 -> 2 month after this month
                data.postValue(DateTimeUtil.getMonthAndYear(DateTimeUtil.getFutureStartOfMonth(monthCount)))
                startOfMonth = DateTimeUtil.getFutureStartOfMonth(1)
                endOfMonth = DateTimeUtil.getFutureEndOfMonth(1)
            }
        }
        viewModelScope.launch(Dispatchers.IO){
            getTransaction()
        }
        return data
    }
}