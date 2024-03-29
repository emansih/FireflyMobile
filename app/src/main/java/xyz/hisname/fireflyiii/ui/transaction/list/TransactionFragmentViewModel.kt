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

package xyz.hisname.fireflyiii.ui.transaction.list

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.repository.models.transaction.SplitSeparator
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionAmountMonth
import xyz.hisname.fireflyiii.repository.transaction.TransactionPagingSource
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.insertDateSeparator
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.transaction.DeleteTransactionWorker

class TransactionFragmentViewModel(application: Application): BaseViewModel(application) {

    private val transactionService by lazy { genericService().create(TransactionService::class.java) }
    private val transactionDataDao = AppDatabase.getInstance(application, getUniqueHash()).transactionDataDao()
    private val transactionRepository = TransactionRepository(transactionDataDao, transactionService)
    private val currencyRepository = CurrencyRepository(
            AppDatabase.getInstance(application, getUniqueHash()).currencyDataDao(),
            genericService().create(CurrencyService::class.java)
    )

    // https://proandroiddev.com/how-to-use-the-paging-3-library-in-android-part-2-e2011070a37d
    fun getTransactionList(startDate: String, endDate: String,
                           transactionType: String): LiveData<PagingData<SplitSeparator>> {
        return Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)) {
            TransactionPagingSource(transactionService, transactionDataDao, startDate, endDate, transactionType)
        }.flow.insertDateSeparator().cachedIn(viewModelScope).asLiveData()
    }

    fun getTransactionAmount(transactionType: String): LiveData<List<TransactionAmountMonth>> {
        val transactionData: MutableLiveData<List<TransactionAmountMonth>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            val currencyAttributes = currencyRepository.defaultCurrency().currencyAttributes
            val currencyCode = currencyAttributes.code
            val currencySymbol = currencyAttributes.symbol
            val currentMonth = transactionRepository.getTransactionByDateAndCurrencyCode(DateTimeUtil.getStartOfMonth(),
                    DateTimeUtil.getEndOfMonth(), currencyCode, transactionType)
            val oneMonthAgo = transactionRepository.getTransactionByDateAndCurrencyCode(DateTimeUtil.getStartOfMonth(1),
                    DateTimeUtil.getEndOfMonth(1), currencyCode, transactionType)
            val twoMonthAgo = transactionRepository.getTransactionByDateAndCurrencyCode(DateTimeUtil.getStartOfMonth(2),
                    DateTimeUtil.getEndOfMonth(2), currencyCode, transactionType)
            val threeMonthAgo = transactionRepository.getTransactionByDateAndCurrencyCode(DateTimeUtil.getStartOfMonth(3),
                    DateTimeUtil.getEndOfMonth(3), currencyCode, transactionType)
            val fourMonthAgo = transactionRepository.getTransactionByDateAndCurrencyCode(DateTimeUtil.getStartOfMonth(4),
                    DateTimeUtil.getEndOfMonth(4), currencyCode, transactionType)
            val fiveMonthAgo = transactionRepository.getTransactionByDateAndCurrencyCode(DateTimeUtil.getStartOfMonth(5),
                    DateTimeUtil.getEndOfMonth(5), currencyCode, transactionType)
            val arrayOfAmount = arrayListOf<TransactionAmountMonth>()
            arrayOfAmount.add(TransactionAmountMonth(
                    DateTimeUtil.getMonthAndYear(DateTimeUtil.getStartOfMonth()),
                    "$currencySymbol $currentMonth",
                    transactionRepository.transactionCount(DateTimeUtil.getStartOfMonth(),
                            DateTimeUtil.getEndOfMonth(), transactionType), transactionType))
            arrayOfAmount.add(TransactionAmountMonth(
                    DateTimeUtil.getMonthAndYear(DateTimeUtil.getStartOfMonth(1)),
                    "$currencySymbol $oneMonthAgo",
                    transactionRepository.transactionCount(DateTimeUtil.getStartOfMonth(1),
                            DateTimeUtil.getEndOfMonth(1), transactionType), transactionType))
            arrayOfAmount.add(TransactionAmountMonth(
                    DateTimeUtil.getMonthAndYear(DateTimeUtil.getStartOfMonth(2)),
                    "$currencySymbol $twoMonthAgo",
                    transactionRepository.transactionCount(DateTimeUtil.getStartOfMonth(2),
                            DateTimeUtil.getEndOfMonth(2), transactionType), transactionType))
            arrayOfAmount.add(TransactionAmountMonth(
                    DateTimeUtil.getMonthAndYear(DateTimeUtil.getStartOfMonth(3)),
                    "$currencySymbol $threeMonthAgo",
                    transactionRepository.transactionCount(DateTimeUtil.getStartOfMonth(3),
                            DateTimeUtil.getEndOfMonth(3), transactionType), transactionType))
            arrayOfAmount.add( TransactionAmountMonth(
                    DateTimeUtil.getMonthAndYear(DateTimeUtil.getStartOfMonth(4)),
                    "$currencySymbol $fourMonthAgo",
                    transactionRepository.transactionCount(DateTimeUtil.getStartOfMonth(4),
                            DateTimeUtil.getEndOfMonth(4), transactionType), transactionType))
            arrayOfAmount.add(TransactionAmountMonth(
                    DateTimeUtil.getMonthAndYear(DateTimeUtil.getStartOfMonth(5)),
                    "$currencySymbol $fiveMonthAgo",
                    transactionRepository.transactionCount(DateTimeUtil.getStartOfMonth(5),
                            DateTimeUtil.getEndOfMonth(5), transactionType), transactionType))
            transactionData.postValue(arrayOfAmount)
        }
        return transactionData
    }

    fun deleteTransaction(transactionJournalId: String): MutableLiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            val transactionId = transactionRepository.getTransactionIdFromJournalId(transactionJournalId.toLong())
            if(transactionId != 0L){
                when (transactionRepository.deleteTransactionById(transactionId)) {
                    HttpConstants.FAILED -> {
                        isDeleted.postValue(false)
                        DeleteTransactionWorker.setupWorker(transactionJournalId.toLong(), getApplication(), getUniqueHash())
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