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

package xyz.hisname.fireflyiii.repository.category

import com.squareup.moshi.Moshi
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.CategoryDataDao
import xyz.hisname.fireflyiii.data.local.dao.TransactionDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.data.remote.firefly.api.SearchService
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.category.CategoryAttributes
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.category.CategorySuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionIndex
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.network.HttpConstants
import java.math.BigDecimal

@Suppress("RedundantSuspendModifier")
class CategoryRepository(private val categoryDao: CategoryDataDao,
                         private val categoryService: CategoryService,
                         private val transactionDao: TransactionDataDao? = null,
                         private val searchService: SearchService? = null){


    suspend fun searchCategoryByName(categoryName: String): List<CategoryData> {
        try {
            checkNotNull(searchService)
            val networkCall = searchService.searchCategories(categoryName)
            val responseBody = networkCall.body()
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
            val networkResponse = categoryService.getTransactionByCategory( categoryId, 1,
                    startDate, endDate, transactionType)
            val transactionData: MutableList<TransactionData> = mutableListOf()
            val responseBody = networkResponse.body()
            if (responseBody != null && networkResponse.isSuccessful) {
                transactionData.addAll(responseBody.data)
                if(responseBody.meta.pagination.total_pages > 1){
                    for (items in 2..responseBody.meta.pagination.total_pages) {
                        val anotherNetworkCall =
                                categoryService.getTransactionByCategory(categoryId, items,
                                        startDate, endDate, transactionType)
                        val anotherResponseBody = anotherNetworkCall.body()
                        if(anotherResponseBody != null && anotherNetworkCall.isSuccessful){
                            transactionData.addAll(anotherResponseBody.data)
                        }
                    }
                }
                transactionDao.deleteTransactionsByDate(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                        DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), transactionType, false)
                transactionData.forEach { data ->
                    data.transactionAttributes.transactions.forEach { transactions ->
                        transactionDao.insert(transactions)
                        transactionDao.insert(TransactionIndex(0, data.transactionId,
                                transactions.transaction_journal_id,
                                data.transactionAttributes.group_title))
                    }
                }
            }
        } catch (exception: Exception){ }
        return transactionDao.getTransactionValueByDateAndCategory(DateTimeUtil.getStartOfDayInCalendarToEpoch(startDate),
                DateTimeUtil.getEndOfDayInCalendarToEpoch(endDate), transactionType, categoryId)
    }

    suspend fun deleteCategoryById(categoryId: Long): Int{
        try {
            val networkResponse = categoryService.deleteCategoryById(categoryId)
            when(networkResponse.code()) {
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

    suspend fun addCategory(categoryName: String): ApiResponses<CategorySuccessModel>{
        return try {
            val networkCall = categoryService.addCategory(categoryName)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }

    suspend fun updateCategory(categoryId: Long, categoryName: String): ApiResponses<CategorySuccessModel> {
        return try {
            val networkCall = categoryService.updateCategory(categoryId, categoryName)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }

    private suspend fun parseResponse(responseFromServer: Response<CategorySuccessModel>): ApiResponses<CategorySuccessModel>{
        val responseBody = responseFromServer.body()
        val responseErrorBody = responseFromServer.errorBody()
        if(responseBody != null && responseFromServer.isSuccessful){
            categoryDao.insert(responseBody.data)
            return ApiResponses(response = responseBody)
        } else {
            if(responseErrorBody != null){
                // Ignore lint warning. False positive
                // https://github.com/square/retrofit/issues/3255#issuecomment-557734546
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(responseErrorBody.source())
                val errorMessage = when {
                    moshi?.errors?.name != null -> moshi.errors.name[0]
                    else -> moshi?.message ?: "Error occurred while saving category"
                }
                return ApiResponses(errorMessage = errorMessage)
            }
            return ApiResponses(errorMessage = "Error occurred while saving category")
        }
    }
}