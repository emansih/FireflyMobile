package xyz.hisname.fireflyiii.ui.transaction.list

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionAmountMonth
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.repository.transaction.TransactionPagingSource
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.DateTimeUtil

class TransactionFragmentViewModel(application: Application): BaseViewModel(application) {

    private val transactionService by lazy { genericService()?.create(TransactionService::class.java) }
    private val transactionDataDao = AppDatabase.getInstance(application).transactionDataDao()
    private val transactionRepository = TransactionRepository(transactionDataDao, transactionService)
    private val currencyRepository = CurrencyRepository(
            AppDatabase.getInstance(application).currencyDataDao(),
            genericService()?.create(CurrencyService::class.java)
    )

    fun getTransactionList(startDate: String?, endDate: String?,
                           transactionType: String): LiveData<PagingData<Transactions>> {
        return Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)) {
            TransactionPagingSource(transactionService, transactionDataDao, startDate, endDate, transactionType)
        }.flow.cachedIn(viewModelScope).asLiveData()
    }

    fun getTransactionAmount(transactionType: String): LiveData<List<TransactionAmountMonth>> {
        val transactionData: MutableLiveData<List<TransactionAmountMonth>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            val currencyAttributes = currencyRepository.defaultCurrency()[0].currencyAttributes
            val currencyCode = currencyAttributes?.code ?: ""
            val currencySymbol = currencyAttributes?.symbol
            val currentMonth = transactionRepository.getTotalTransactionType(DateTimeUtil.getStartOfMonth(),
                    DateTimeUtil.getEndOfMonth(), currencyCode, transactionType)
            val oneMonthAgo = transactionRepository.getTotalTransactionType(DateTimeUtil.getStartOfMonth(1),
                    DateTimeUtil.getEndOfMonth(1), currencyCode, transactionType)
            val twoMonthAgo = transactionRepository.getTotalTransactionType(DateTimeUtil.getStartOfMonth(2),
                    DateTimeUtil.getEndOfMonth(2), currencyCode, transactionType)
            val threeMonthAgo = transactionRepository.getTotalTransactionType(DateTimeUtil.getStartOfMonth(3),
                    DateTimeUtil.getEndOfMonth(3), currencyCode, transactionType)
            val fourMonthAgo = transactionRepository.getTotalTransactionType(DateTimeUtil.getStartOfMonth(4),
                    DateTimeUtil.getEndOfMonth(4), currencyCode, transactionType)
            val fiveMonthAgo = transactionRepository.getTotalTransactionType(DateTimeUtil.getStartOfMonth(5),
                    DateTimeUtil.getEndOfMonth(5), currencyCode, transactionType)
            val arrayOfAmount = arrayListOf<TransactionAmountMonth>()
            arrayOfAmount.add(TransactionAmountMonth(
                    DateTimeUtil.getMonthAndYear(DateTimeUtil.getStartOfMonth()),
                    "$currencySymbol $currentMonth",
                    transactionRepository.transactionCount(DateTimeUtil.getStartOfMonth(),
                            DateTimeUtil.getEndOfMonth(), transactionType)))
            arrayOfAmount.add(TransactionAmountMonth(
                    DateTimeUtil.getMonthAndYear(DateTimeUtil.getStartOfMonth(1)),
                    "$currencySymbol $oneMonthAgo",
                    transactionRepository.transactionCount(DateTimeUtil.getStartOfMonth(1),
                            DateTimeUtil.getEndOfMonth(1), transactionType)))
            arrayOfAmount.add(TransactionAmountMonth(
                    DateTimeUtil.getMonthAndYear(DateTimeUtil.getStartOfMonth(2)),
                    "$currencySymbol $twoMonthAgo",
                    transactionRepository.transactionCount(DateTimeUtil.getStartOfMonth(2),
                            DateTimeUtil.getEndOfMonth(2), transactionType)))
            arrayOfAmount.add(TransactionAmountMonth(
                    DateTimeUtil.getMonthAndYear(DateTimeUtil.getStartOfMonth(3)),
                    "$currencySymbol $threeMonthAgo",
                    transactionRepository.transactionCount(DateTimeUtil.getStartOfMonth(3),
                            DateTimeUtil.getEndOfMonth(3), transactionType)))
            arrayOfAmount.add( TransactionAmountMonth(
                    DateTimeUtil.getMonthAndYear(DateTimeUtil.getStartOfMonth(4)),
                    "$currencySymbol $fourMonthAgo",
                    transactionRepository.transactionCount(DateTimeUtil.getStartOfMonth(4),
                            DateTimeUtil.getEndOfMonth(4), transactionType)))
            arrayOfAmount.add(TransactionAmountMonth(
                    DateTimeUtil.getMonthAndYear(DateTimeUtil.getStartOfMonth(5)),
                    "$currencySymbol $fiveMonthAgo",
                    transactionRepository.transactionCount(DateTimeUtil.getStartOfMonth(5),
                            DateTimeUtil.getEndOfMonth(5), transactionType)))
            transactionData.postValue(arrayOfAmount)
        }
        return transactionData
    }
}