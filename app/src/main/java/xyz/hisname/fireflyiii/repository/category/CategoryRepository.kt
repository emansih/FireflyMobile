package xyz.hisname.fireflyiii.repository.category

import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.CategoryDataDao
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.repository.models.category.CategoryAttributes
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.category.CategorySuccessModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.network.HttpConstants
import java.math.BigDecimal

@Suppress("RedundantSuspendModifier")
class CategoryRepository(private val categoryDao: CategoryDataDao,
                         private val categoryService: CategoryService?,
                         private val transactionDao: TransactionDataDao? = null){


    suspend fun insertCategory(category: CategoryData) = categoryDao.insert(category)

    suspend fun searchCategoryByName(categoryName: String): List<CategoryData> {
        try {
            val networkCall = categoryService?.searchCategory(categoryName)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                responseBody.forEach { category ->
                    categoryDao.deleteCategoryById(category.id)
                    categoryDao.insert(CategoryData(category.id, CategoryAttributes("", category.name, "", "")))
                }
            }
        } catch (exception: Exception){ }
        return categoryDao.searchCategory("*$categoryName*")
    }

    suspend fun getCategoryById(categoryId: Long) = categoryDao.getCategoryById(categoryId)

    suspend fun getTransactionValueFromCategory(categoryId: Long, startDate: String,
                                                endDate: String, transactionType: String): BigDecimal{
        checkNotNull(transactionDao)
        try {
            val networkResponse = categoryService?.getTransactionByCategory( categoryId, 1,
                    startDate, endDate, transactionType)
            val transactionData: MutableList<TransactionData> = mutableListOf()
            val responseBody = networkResponse?.body()
            if (responseBody != null && networkResponse.isSuccessful) {
                transactionData.addAll(responseBody.data)
                if(responseBody.meta.pagination.total_pages > 1){
                    for (items in 2..responseBody.meta.pagination.total_pages) {
                        val anotherNetworkCall =
                                categoryService?.getTransactionByCategory(categoryId, items,
                                        startDate, endDate, transactionType)
                        val anotherResponseBody = anotherNetworkCall?.body()
                        if(anotherResponseBody != null && anotherNetworkCall.isSuccessful){
                            transactionData.addAll(anotherResponseBody.data)
                        }
                    }
                }
                transactionDao.deleteTransactionsByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                        DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), transactionType, false)
                transactionData.forEach { data ->
                    data.transactionAttributes?.transactions?.forEach { transactions ->
                        transactionDao.insert(transactions)
                        transactionDao.insert(TransactionIndex(data.transactionId, transactions.transaction_journal_id))
                    }
                }
            }
        } catch (exception: Exception){ }
        return transactionDao.getTransactionValueByDateAndCategory(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), transactionType, categoryId)
    }

    suspend fun deleteCategoryById(categoryId: Long): Int{
        try {
            val networkResponse = categoryService?.deleteCategoryById(categoryId)
            when(networkResponse?.code()) {
                204 -> {
                    categoryDao.deleteCategoryById(categoryId)
                    return HttpConstants.NO_CONTENT_SUCCESS
                }
                401 -> {
                    /*   User is unauthenticated. We will retain user's data as we are
                     *   now in inconsistent state. This use case is unlikely to happen unless user
                     *   deletes their token from the web interface without updating the mobile client
                     */
                    return HttpConstants.UNAUTHORISED
                }
                404 -> {
                    // User probably deleted this on the web interface and tried to do it using mobile client
                    categoryDao.deleteCategoryById(categoryId)
                    return HttpConstants.NOT_FOUND
                }
                else -> {
                    return HttpConstants.FAILED
                }
            }
        } catch (exception: Exception){
            categoryDao.deleteCategoryById(categoryId)
            return HttpConstants.FAILED
        }
    }

    @Throws(Exception::class)
    suspend fun updateCategory(categoryId: Long, categoryName: String): Response<CategorySuccessModel>? {
        val networkCall = categoryService?.updateCategory(categoryId, categoryName)
        val responseBody = networkCall?.body()
        if (responseBody != null && networkCall.isSuccessful) {
            insertCategory(responseBody.data)
        }
        return networkCall
    }
}