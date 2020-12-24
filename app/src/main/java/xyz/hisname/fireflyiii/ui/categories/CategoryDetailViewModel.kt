package xyz.hisname.fireflyiii.ui.categories

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
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.category.CategoryRepository
import xyz.hisname.fireflyiii.repository.category.TransactionPagingSource
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.transaction.SplitSeparator
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.workers.category.DeleteCategoryWorker

class CategoryDetailViewModel(application: Application): BaseViewModel(application) {

    private val categoryService = genericService().create(CategoryService::class.java)
    private val categoryDao = AppDatabase.getInstance(application).categoryDataDao()
    private val transactionDao = AppDatabase.getInstance(application).transactionDataDao()
    private val categoryRepository = CategoryRepository(categoryDao, categoryService, transactionDao)
    private var catId: Long = 0L
    val withdrawData: MutableLiveData<List<Float>> = MutableLiveData()
    val depositData: MutableLiveData<List<Float>> = MutableLiveData()

    fun getCategoryById(categoryId: Long): LiveData<CategoryData>{
        catId = categoryId
        val categoryLiveData = MutableLiveData<CategoryData>()
        viewModelScope.launch(Dispatchers.IO){
            categoryLiveData.postValue(categoryRepository.getCategoryById(categoryId))
            getWithdrawalAmount()
            getDepositAmount()
        }
        return categoryLiveData
    }

    fun getTransactionList(): LiveData<PagingData<SplitSeparator>>{
        return Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)) {
            TransactionPagingSource(DateTimeUtil.getStartOfDayInCalendarToEpoch(DateTimeUtil.getStartOfMonth()),
                    DateTimeUtil.getEndOfDayInCalendarToEpoch(DateTimeUtil.getEndOfMonth()), catId, transactionDao)
        }.flow.map { pagingData ->
            pagingData.map { transactions ->
                SplitSeparator.TransactionItem(transactions)
            }.insertSeparators { before, after ->
                if (before == null) {
                    if(after != null){
                        return@insertSeparators SplitSeparator.SeparatorItem(after.transaction.date.dayOfMonth.toString()
                                + " " + after.transaction.date.month + " "
                                + after.transaction.date.year)
                    }
                    return@insertSeparators null
                }
                if(after == null){
                    return@insertSeparators null
                }

                if (after.transaction.date.isAfter(before.transaction.date)) {
                    SplitSeparator.SeparatorItem(after.transaction.date.dayOfMonth.toString()
                            + " " + after.transaction.date.month + " "
                            + after.transaction.date.year)
                } else {
                    null
                }

            }
        }.cachedIn(viewModelScope).asLiveData()
    }

    fun deleteCategory(): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            when (categoryRepository.deleteCategoryById(catId)) {
                // Don't ask me why... It will throw an exception even though it is successful
                HttpConstants.FAILED -> {
                    isDeleted.postValue(false)
                    DeleteCategoryWorker.initPeriodicWorker(catId, getApplication())
                }
                HttpConstants.UNAUTHORISED -> {
                    isDeleted.postValue(false)
                }
                HttpConstants.NO_CONTENT_SUCCESS -> {
                    isDeleted.postValue(true)
                }
            }
        }
        return isDeleted
    }

    private suspend fun getWithdrawalAmount(){
        val firstDayOfMonth = categoryRepository.getTransactionValueFromCategory(catId,
                DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getStartOfMonth(), "withdrawal")

        val secondWeekOfMonth = categoryRepository.getTransactionValueFromCategory(catId,
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 1),
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 1), "withdrawal")

        val thirdWeekOfMonth = categoryRepository.getTransactionValueFromCategory(catId,
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 2),
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 2), "withdrawal")

        val fourthWeekOfMonth = categoryRepository.getTransactionValueFromCategory(catId,
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 3),
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 3), "withdrawal")

        val lastDayOfMonth = categoryRepository.getTransactionValueFromCategory(catId,
                DateTimeUtil.getEndOfMonth(),
                DateTimeUtil.getEndOfMonth(), "withdrawal")

        withdrawData.postValue(listOf(
                firstDayOfMonth.toFloat(),
                secondWeekOfMonth.toFloat(),
                thirdWeekOfMonth.toFloat(),
                fourthWeekOfMonth.toFloat(),
                lastDayOfMonth.toFloat()
        ))
    }

    private suspend fun getDepositAmount(){
        val firstDayOfMonth = categoryRepository.getTransactionValueFromCategory(catId,
                DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getStartOfMonth(), "deposit")

        val secondWeekOfMonth = categoryRepository.getTransactionValueFromCategory(catId,
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 1),
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 1), "deposit")

        val thirdWeekOfMonth = categoryRepository.getTransactionValueFromCategory(catId,
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 2),
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 2), "deposit")

        val fourthWeekOfMonth = categoryRepository.getTransactionValueFromCategory(catId,
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 3),
                DateTimeUtil.getStartOfWeekFromGivenDate(DateTimeUtil.getStartOfMonth(), 3), "deposit")

        val lastDayOfMonth = categoryRepository.getTransactionValueFromCategory(catId,
                DateTimeUtil.getEndOfMonth(),
                DateTimeUtil.getEndOfMonth(), "deposit")

        depositData.postValue(listOf(
                firstDayOfMonth.toFloat(),
                secondWeekOfMonth.toFloat(),
                thirdWeekOfMonth.toFloat(),
                fourthWeekOfMonth.toFloat(),
                lastDayOfMonth.toFloat()
        ))
    }
}