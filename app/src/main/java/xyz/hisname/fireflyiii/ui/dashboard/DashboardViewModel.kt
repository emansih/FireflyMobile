package xyz.hisname.fireflyiii.ui.dashboard

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.SimpleData
import xyz.hisname.fireflyiii.data.remote.firefly.api.BudgetService
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.data.remote.firefly.api.SummaryService
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.budget.BudgetRepository
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.repository.transaction.TransactionLimitSource
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.Sixple
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import java.math.BigDecimal

class DashboardViewModel(application: Application): BaseViewModel(application) {

    private val currencyRepository = CurrencyRepository(
            AppDatabase.getInstance(application).currencyDataDao(),
            genericService().create(CurrencyService::class.java))
    private val budgetRepository = BudgetRepository(
            AppDatabase.getInstance(application).budgetDataDao(),
            AppDatabase.getInstance(application).budgetListDataDao(),
            AppDatabase.getInstance(application).spentDataDao(),
            AppDatabase.getInstance(application).budgetLimitDao(),
            genericService().create(BudgetService::class.java)
    )

    private val transactionDao = AppDatabase.getInstance(application).transactionDataDao()
    private val transactionRepository = TransactionRepository(
            transactionDao, genericService().create(TransactionService::class.java)
    )

    private lateinit var currencyCode: String
    private var currencySymbolLocationIsAtEnd = false
    val currencySymbol: MutableLiveData<String> = MutableLiveData()
    val networthValue: MutableLiveData<String> = MutableLiveData()
    val leftToSpendValue: MutableLiveData<String> = MutableLiveData()
    val balanceValue: MutableLiveData<String> = MutableLiveData()
    val earnedValue: MutableLiveData<String> = MutableLiveData()
    val spentValue: MutableLiveData<String> = MutableLiveData()
    val billsToPay: MutableLiveData<String> = MutableLiveData()
    val billsPaid: MutableLiveData<String> = MutableLiveData()
    val leftToSpendDay: MutableLiveData<String> = MutableLiveData()
    val currentMonthSpentValue: MutableLiveData<String> = MutableLiveData()
    val currentMonthBudgetValue: MutableLiveData<String> = MutableLiveData()
    val budgetLeftPercentage: MutableLiveData<BigDecimal> = MutableLiveData()
    val budgetSpentPercentage: MutableLiveData<BigDecimal> = MutableLiveData()
    val currentMonthWithdrawalLiveData: MutableLiveData<BigDecimal> = MutableLiveData()
    val currentMonthDepositLiveData: MutableLiveData<BigDecimal> = MutableLiveData()
    val lastMonthWithdrawalLiveData: MutableLiveData<BigDecimal> = MutableLiveData()
    val lastMonthDepositLiveData: MutableLiveData<BigDecimal> = MutableLiveData()
    val twoMonthsAgoDepositLiveData: MutableLiveData<BigDecimal> = MutableLiveData()
    val twoMonthsAgoWithdrawalLiveData: MutableLiveData<BigDecimal> = MutableLiveData()
    val sixDayWithdrawalLiveData: MutableLiveData<Sixple<BigDecimal, BigDecimal,
            BigDecimal, BigDecimal, BigDecimal, BigDecimal>> = MutableLiveData()
    var currentMonthNetBigDecimal = 0.toBigDecimal()
        private set
    var lastMonthNetBigDecimal = 0.toBigDecimal()
        private set
    var twoMonthAgoNetBigDecimal = 0.toBigDecimal()
        private set
    var currentMonthNetString = ""
        private set
    var lastMonthNetString = ""
        private set
    var twoMonthAgoNetString = ""
        private set
    var sixDaysAverage: String = ""
        private set
    var thirtyDayAverage: String = ""
        private set
    var currentMonthWithdrawal: String = ""
        private set
    var currentMonthDeposit: String = ""
        private set
    var lastMonthWithdrawal: String = ""
        private set
    var lastMonthDeposit: String = ""
        private set
    var twoMonthsAgoDeposit: String = ""
        private set
    var twoMonthsAgoWithdrawal: String = ""
        private set


    init {
        getDefaultCurrency()
    }


    private fun getDefaultCurrency(){
        viewModelScope.launch(Dispatchers.IO){
            val currencyList = currencyRepository.defaultCurrency()
            if(currencyList.currencyId == null){
                apiResponse.postValue("There was an issue getting your default currency")
            } else {
                currencySymbol.postValue(currencyList.currencyAttributes?.symbol)
                currencyCode = currencyList.currencyAttributes?.code ?: ""
                getBasicSummary(currencyList.currencyAttributes?.code ?: "", currencyList.currencyAttributes?.symbol ?: "")
                getPieChartData(currencyList.currencyAttributes?.code ?: "", currencyList.currencyAttributes?.symbol ?: "")
                setNetEarnings(currencyList.currencyAttributes?.code ?: "", currencyList.currencyAttributes?.symbol ?: "")

            }
        }
    }


    private fun getBasicSummary(currencyCode: String, currencySymbol: String){
        val simpleData = SimpleData(PreferenceManager.getDefaultSharedPreferences(getApplication()))
        val summaryService = genericService().create(SummaryService::class.java)
        summaryService.getBasicSummary(DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth(),
                currencyCode).enqueue(retrofitCallback({ response ->
            val responseBody = response.body()
            if (response.isSuccessful && responseBody != null) {
                // so dirty I went to take a shower after writing this code
                val netWorth = try {
                    responseBody
                            .getJSONObject("net-worth-in-$currencyCode")
                            .getString("value_parsed")
                } catch (exception: Exception){
                    "0.0"
                }
                simpleData.networthValue = netWorth
                if(netWorth?.indexOf(currencySymbol) != 0){
                    currencySymbolLocationIsAtEnd = true
                }
                val leftToSpend = try {
                    responseBody
                            .getJSONObject("left-to-spend-in-$currencyCode")
                            .getString("value_parsed")
                } catch (exception: Exception){
                    "0.0"
                }
                simpleData.leftToSpend = leftToSpend

                val balance = try {
                    responseBody
                            .getJSONObject("balance-in-$currencyCode")
                            .getString("value_parsed")
                } catch(exception: Exception){
                    "0.0"
                }
                simpleData.balance = balance

                val earned = try {
                    responseBody
                            .getJSONObject("earned-in-$currencyCode")
                            .getString("value_parsed")
                } catch(exception: Exception){
                    "0.0"
                }
                simpleData.earned = earned

                val spent = try {
                    responseBody
                            .getJSONObject("spent-in-$currencyCode")
                            .getString("value_parsed")
                } catch(exception: Exception){
                    "0.0"
                }
                simpleData.spent = spent

                val unPaidBills = try {
                    responseBody
                            .getJSONObject("bills-unpaid-in-$currencyCode")
                            .getString("value_parsed")
                } catch(exception: Exception){
                    "0.0"
                }
                simpleData.unPaidBills = unPaidBills

                val paidBills = try {
                    responseBody
                            .getJSONObject("bills-paid-in-$currencyCode")
                            .getString("value_parsed")
                } catch(exception: Exception){
                    "0.0"
                }
                simpleData.paidBills = paidBills

                val leftToSpendPerDay = try {
                    responseBody
                            .getJSONObject("left-to-spend-in-$currencyCode")
                            .getString("sub_title")
                } catch(exception: Exception){
                    "0.0"
                }
                val formattedText = leftToSpendPerDay.replace("Left to spend per day: ", "")
                simpleData.leftToSpendPerDay = formattedText
            }
            networthValue.postValue(simpleData.networthValue)
            leftToSpendValue.postValue(simpleData.leftToSpend)
            balanceValue.postValue(simpleData.balance)
            earnedValue.postValue(simpleData.earned)
            spentValue.postValue(simpleData.spent)
            billsToPay.postValue(simpleData.unPaidBills)
            billsPaid.postValue(simpleData.paidBills)
            leftToSpendDay.postValue(simpleData.leftToSpendPerDay)
        })
        { throwable ->
            if(simpleData.networthValue?.indexOf(currencySymbol) != 0){
                currencySymbolLocationIsAtEnd = true
            }
            networthValue.postValue(simpleData.networthValue)
            leftToSpendValue.postValue(simpleData.leftToSpend)
            balanceValue.postValue(simpleData.balance)
            earnedValue.postValue(simpleData.earned)
            spentValue.postValue(simpleData.spent)
            billsToPay.postValue(simpleData.unPaidBills)
            billsPaid.postValue(simpleData.paidBills)
            leftToSpendDay.postValue(simpleData.leftToSpendPerDay)
        })
    }

    private suspend fun getPieChartData(currencyCode: String, currencySymbol: String){
        val budgetSpent = budgetRepository.allActiveSpentList(currencyCode,
                DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth())
        if(currencySymbolLocationIsAtEnd){
            currentMonthSpentValue.postValue(budgetSpent.toString() + currencySymbol)
        } else {
            currentMonthSpentValue.postValue(currencySymbol + budgetSpent)
        }
        budgetRepository.getAllBudget()
        val budgeted = budgetRepository.retrieveConstraintBudgetWithCurrency(
                DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), currencyCode)
        if(currencySymbolLocationIsAtEnd){
            currentMonthBudgetValue.postValue(budgeted.toString() + currencySymbol)
        } else {
            currentMonthBudgetValue.postValue(currencySymbol + budgeted)
        }

        if(budgetSpent == BigDecimal.ZERO){
            budgetLeftPercentage.postValue(BigDecimal.ZERO)
        } else {
            if (budgeted == BigDecimal.ZERO){
                budgetSpentPercentage.postValue(BigDecimal.ZERO)
            } else {
                budgetLeftPercentage.postValue(
                        budgetSpent.divide(budgeted, BigDecimal.ROUND_HALF_EVEN)
                                .multiply(100.toBigDecimal())
                                .setScale(2, BigDecimal.ROUND_HALF_DOWN))
                budgetSpentPercentage.postValue(
                        budgeted.minus(budgetSpent)
                                .divide(budgeted, BigDecimal.ROUND_HALF_EVEN)
                                .multiply(100.toBigDecimal())
                                .setScale(2, BigDecimal.ROUND_HALF_DOWN))
            }
        }
    }

    private suspend fun setNetEarnings(currencyCode: String, currencySymbol: String){
        val currentWithdrawal = transactionRepository.getTransactionByDateAndCurrencyCode(
                DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), currencyCode, "withdrawal",true)
        val currentDeposit = transactionRepository.getTransactionByDateAndCurrencyCode(
                DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), currencyCode, "deposit",true)
        val lastMonthWithdraw = transactionRepository.getTransactionByDateAndCurrencyCode(
                DateTimeUtil.getStartOfMonth(1),
                DateTimeUtil.getEndOfMonth(1), currencyCode, "withdrawal",true)
        val lastMonthDep = transactionRepository.getTransactionByDateAndCurrencyCode(
                DateTimeUtil.getStartOfMonth(1),
                DateTimeUtil.getEndOfMonth(1), currencyCode, "deposit",true)
        val twoMonthsAgoWithdraw = transactionRepository.getTransactionByDateAndCurrencyCode(
                DateTimeUtil.getStartOfMonth(2),
                DateTimeUtil.getEndOfMonth(2), currencyCode, "withdrawal",true)
        val twoMonthsAgoDep = transactionRepository.getTransactionByDateAndCurrencyCode(
                DateTimeUtil.getStartOfMonth(2),
                DateTimeUtil.getEndOfMonth(2), currencyCode, "deposit",true)
        currentMonthNetBigDecimal = currentDeposit - currentWithdrawal
        currentMonthNetString = if(currencySymbolLocationIsAtEnd){
            currentMonthNetBigDecimal.toString() + currencySymbol
        } else {
            currencySymbol + currentMonthNetBigDecimal.toString()
        }
        lastMonthNetBigDecimal = lastMonthDep - lastMonthWithdraw
        lastMonthNetString = if(currencySymbolLocationIsAtEnd){
            lastMonthNetBigDecimal.toString() + currencySymbol
        } else {
            currencySymbol + lastMonthNetBigDecimal.toString()
        }
        twoMonthAgoNetBigDecimal = twoMonthsAgoDep - twoMonthsAgoWithdraw
        twoMonthAgoNetString = if(currencySymbolLocationIsAtEnd){
            twoMonthAgoNetBigDecimal.toString() + currencySymbol
        } else {
            currencySymbol + twoMonthAgoNetBigDecimal.toString()
        }
        thirtyDayAverage = if(currencySymbolLocationIsAtEnd){
            currentWithdrawal.div(30.toBigDecimal()).toString() + currencySymbol
        } else {
            currencySymbol + currentWithdrawal.div(30.toBigDecimal()).toString()
        }
        currentMonthWithdrawal = if(currencySymbolLocationIsAtEnd){
            currentWithdrawal.toString() + currencySymbol
        } else {
            currencySymbol + currentWithdrawal.toString()
        }
        currentMonthDeposit = if(currencySymbolLocationIsAtEnd){
            currentDeposit.toString() + currencySymbol
        } else {
            currencySymbol + currentDeposit.toString()
        }
        lastMonthWithdrawal = if(currencySymbolLocationIsAtEnd){
            lastMonthWithdraw.toString() + currencySymbol
        } else {
            currencySymbol + lastMonthWithdraw.toString()
        }
        lastMonthDeposit = if(currencySymbolLocationIsAtEnd){
            lastMonthDep.toString() + currencySymbol
        } else {
            currencySymbol + lastMonthDep.toString()
        }
        twoMonthsAgoDeposit = if(currencySymbolLocationIsAtEnd){
            twoMonthsAgoDep.toString() + currencySymbol
        } else {
            currencySymbol + twoMonthsAgoDep.toString()
        }
        twoMonthsAgoWithdrawal = if(currencySymbolLocationIsAtEnd){
            twoMonthsAgoWithdraw.toString() + currencySymbol
        } else {
            currencySymbol + twoMonthsAgoWithdraw.toString()
        }
        currentMonthWithdrawalLiveData.postValue(currentWithdrawal)
        currentMonthDepositLiveData.postValue(currentDeposit)
        lastMonthWithdrawalLiveData.postValue(lastMonthWithdraw)
        lastMonthDepositLiveData.postValue(lastMonthDep)
        twoMonthsAgoDepositLiveData.postValue(twoMonthsAgoDep)
        twoMonthsAgoWithdrawalLiveData.postValue(twoMonthsAgoWithdraw)

        setDailySummary(currencySymbol)
    }


    private suspend fun setDailySummary(currencySymbol: String){
        val yesterdayWith = transactionRepository.getTransactionByDateAndCurrencyCode(
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 1),
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 1), currencyCode,
                "withdrawal",false)
        val twoDaysAgoWith = transactionRepository.getTransactionByDateAndCurrencyCode(
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 2),
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 2), currencyCode,
                "withdrawal",false)
        val threeDaysAgoWith = transactionRepository.getTransactionByDateAndCurrencyCode(
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 3),
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 3), currencyCode,
                "withdrawal",false)
        val fourDaysAgoWith = transactionRepository.getTransactionByDateAndCurrencyCode(
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 4),
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 4), currencyCode,
                "withdrawal",false)
        val fiveDaysAgoWith = transactionRepository.getTransactionByDateAndCurrencyCode(
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 5),
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 5), currencyCode,
                "withdrawal",false)
        val sixDaysAgoWith = transactionRepository.getTransactionByDateAndCurrencyCode(
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6),
                DateTimeUtil.getDaysBefore(DateTimeUtil.getTodayDate(), 6), currencyCode,
                "withdrawal",false)
        sixDaysAverage = if(currencySymbolLocationIsAtEnd){
            (yesterdayWith + twoDaysAgoWith + threeDaysAgoWith +
                    fourDaysAgoWith + fiveDaysAgoWith + sixDaysAgoWith).div(6.toBigDecimal()).toString() + currencySymbol
        } else {
            currencySymbol + (yesterdayWith + twoDaysAgoWith + threeDaysAgoWith +
                    fourDaysAgoWith + fiveDaysAgoWith + sixDaysAgoWith).div(6.toBigDecimal()).toString()
        }
        sixDayWithdrawalLiveData.postValue(Sixple(yesterdayWith, twoDaysAgoWith, threeDaysAgoWith,
                fourDaysAgoWith, fiveDaysAgoWith, sixDaysAgoWith))
    }

    fun getRecentTransactions(): LiveData<PagingData<Transactions>> {
        return Pager(PagingConfig(5)) {
            TransactionLimitSource(5, transactionDao)
        }.flow.cachedIn(viewModelScope).asLiveData()
    }
}