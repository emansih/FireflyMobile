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

package xyz.hisname.fireflyiii.repository.budget

import androidx.annotation.WorkerThread
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.*
import xyz.hisname.fireflyiii.data.remote.firefly.api.BudgetService
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.budget.BudgetData
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListData
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListSuccessModel
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetType
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.network.HttpConstants
import xyz.hisname.fireflyiii.util.network.retrofitCallback
import java.math.BigDecimal

@Suppress("RedundantSuspendModifier")
@WorkerThread
class BudgetRepository(private val budget: BudgetDataDao,
                       private val budgetList: BudgetListDataDao,
                       private val spentDao: SpentDataDao,
                       private val budgetLimitDao: BudgetLimitDao,
                       private val budgetService: BudgetService) {

    suspend fun insertBudget(budgetData: BudgetData){
        budget.insert(budgetData)
    }

    suspend fun deleteAllBudget() = budget.deleteAllBudget()

    suspend fun insertBudgetList(budgetData: BudgetListData){
        budgetList.insert(budgetData)
        val spentList = budgetData.budgetListAttributes.spent
        if(spentList.isNotEmpty()){
            spentList.forEach { spent ->
                spent.budgetId = budgetData.budgetListId
                spentDao.insert(spent)
            }
        }
    }

    suspend fun allActiveSpentList(currencyCode: String, startDate: String, endDate: String): BigDecimal{
        try {
            val budgetListData: MutableList<BudgetListData> = arrayListOf()
            val networkCall = budgetService.getPaginatedSpentBudget(1, startDate, endDate)
            val responseBody = networkCall.body()
            if (responseBody != null && networkCall.isSuccessful) {
                budgetListData.addAll(responseBody.data)
                if (responseBody.meta.pagination.current_page != responseBody.meta.pagination.total_pages) {
                    for (pagination in 2..responseBody.meta.pagination.total_pages) {
                        val repeatedCall = budgetService.getPaginatedSpentBudget(pagination, startDate, endDate)
                        val repeatedCallBody = repeatedCall.body()
                        if (repeatedCallBody != null) {
                            budgetListData.addAll(repeatedCallBody.data)
                        }
                    }
                }
                budgetList.deleteAllBudgetList()
                budgetListData.forEach { budgetList ->
                    insertBudgetList(budgetList)
                }
            }
        } catch (exception: Exception){ }
        return spentDao.getAllActiveBudgetList(currencyCode)
    }

    suspend fun getConstraintBudgetWithCurrency(startDate: String, endDate: String,
                                                currencyCode: String) =
            budget.getConstraintBudgetWithCurrency(startDate, endDate, currencyCode)

    suspend fun getBudgetByCurrencyAndStartEndDate(startDate: String, endDate: String,
                                                   currencyCode: String) =
            budget.getBudgetByCurrencyAndStartEndDate(startDate, endDate, currencyCode)

    suspend fun getAllAvailableBudget(startDate: String, endDate: String,
                                      currencyCode: String): BigDecimal {
        try {
            val networkCall = budgetService.getAvailableBudget(1, startDate, endDate)
            val responseBody = networkCall.body()
            val availableBudget: MutableList<BudgetData> = arrayListOf()
            if (responseBody != null && networkCall.isSuccessful) {
                availableBudget.addAll(responseBody.budgetData)
                if (responseBody.meta.pagination.current_page != responseBody.meta.pagination.total_pages) {
                    for (pagination in 2..responseBody.meta.pagination.total_pages) {
                        val repeatedCall = budgetService.getAvailableBudget(pagination, startDate, endDate)
                        val repeatedCallBody = repeatedCall.body()
                        if (repeatedCallBody != null) {
                            availableBudget.addAll(repeatedCallBody.budgetData)
                        }
                    }
                }
                deleteAllBudget()
                availableBudget.forEach { budget ->
                    insertBudget(budget)
                }
            }
        } catch (exception: Exception){ }
        return budget.getConstraintBudgetWithCurrency(startDate, endDate, currencyCode)
    }

    suspend fun updateBudget(budgetId: Long, currencyCode: String, amount: String,
                             startDate: String, endDate: String): BudgetData{
        val networkCall = budgetService.updateAvailableBudget(budgetId, currencyCode, amount, startDate, endDate)
        val responseBody = networkCall.body()
        if (responseBody != null && networkCall.isSuccessful) {
            insertBudget(responseBody.data)
            return responseBody.data
        } else {
            throw Exception("There was an issue updating your budget")
        }
    }

    suspend fun getBudgetLimitByName(budgetName: String, startDate: String, endDate: String, currencySymbol: String): BigDecimal{
        val budgetNameList = budgetList.searchBudgetName(budgetName)
        val budgetId = budgetNameList[0].budgetListId
        try {
            val networkCall = budgetService.getBudgetLimit(budgetId, startDate, endDate)
            val responseBody = networkCall.body()
            // There is no pagination in API
            if (responseBody != null && networkCall.isSuccessful) {
                responseBody.budgetLimitData.forEach {  budgetLimitData ->
                    budgetLimitDao.insert(budgetLimitData)
                }
            }
        } catch (exception: Exception){ }
        return budgetLimitDao.getBudgetLimitByIdAndCurrencyCodeAndDate(budgetId, currencySymbol, startDate, endDate)
    }

    suspend fun getBudgetLimit(budgetId: Long, startDate: String, endDate: String){
        try {
            val networkCall = budgetService.getBudgetLimit(budgetId, startDate, endDate)
            val responseBody = networkCall.body()
            // There is no pagination in API
            if (responseBody != null && networkCall.isSuccessful) {
                responseBody.budgetLimitData.forEach {  budgetLimitData ->
                    budgetLimitDao.insert(budgetLimitData)
                }
            }
        } catch (exception: Exception){ }
    }

    suspend fun getAllBudgetFlow(startDate: String, endDate: String): Flow<List<BudgetListData>> {
        try {
            val budgetListData: MutableList<BudgetListData> = arrayListOf()
            val networkCall = budgetService.getPaginatedSpentBudget(1, startDate, endDate)
            val responseBody = networkCall.body()
            if(responseBody != null && networkCall.isSuccessful){
                budgetListData.addAll(responseBody.data)
                if (responseBody.meta.pagination.current_page != responseBody.meta.pagination.total_pages) {
                    for(pagination in 2..responseBody.meta.pagination.total_pages){
                        val networkBody = budgetService.getPaginatedSpentBudget(pagination, startDate, endDate).body()
                        if(networkBody != null){
                            budgetListData.addAll(networkBody.data)
                        }
                    }
                }
            }
            budgetListData.forEach { data ->
                insertBudgetList(data)
            }
        } catch (exception: Exception){ }
        return budgetList.getAllBudgetFlow()
    }

    suspend fun getAllBudgetList(startDate: String, endDate: String): List<BudgetListData> {
        try {
            val budgetListData: MutableList<BudgetListData> = arrayListOf()
            val networkCall = budgetService.getPaginatedSpentBudget(1, startDate, endDate)
            val responseBody = networkCall.body()
            if(responseBody != null && networkCall.isSuccessful){
                budgetListData.addAll(responseBody.data)
                if (responseBody.meta.pagination.current_page != responseBody.meta.pagination.total_pages) {
                    for(pagination in 2..responseBody.meta.pagination.total_pages){
                        val networkBody = budgetService.getPaginatedSpentBudget(pagination, startDate, endDate).body()
                        if(networkBody != null){
                            budgetListData.addAll(networkBody.data)
                        }
                    }
                }
            }
            budgetListData.forEach { data ->
                insertBudgetList(data)
            }
        } catch (exception: Exception){ }
        return budgetList.getAllBudget()
    }

    suspend fun getAllBudget(){
        try {
            val availableBudget: MutableList<BudgetData> = arrayListOf()
            val networkCall = budgetService.getAllBudget()
            val responseBody = networkCall.body()
            if(responseBody != null && networkCall.isSuccessful){
                deleteAllBudget()
                if (responseBody.meta.pagination.current_page != responseBody.meta.pagination.total_pages) {
                    responseBody.meta.pagination.let { page ->
                        for (pagination in 2..page.total_pages){
                            budgetService.getPaginatedBudget(pagination).enqueue(retrofitCallback({ response ->
                                response.body()?.budgetData?.forEach { budgetList ->
                                    availableBudget.add(budgetList)
                                }
                            }))
                        }
                    }
                }
                responseBody.budgetData.forEach { budgetData ->
                    insertBudget(budgetData)
                }
            }

        } catch (exception: Exception){ }
    }

    suspend fun getBudgetByName(budgetName: String) = budgetList.searchBudgetName(budgetName)

    suspend fun deleteBudgetByName(budgetId: Long): Int{
        try {
            val networkResponse = budgetService.deleteBudgetLimit(budgetId)
            when (networkResponse.code()) {
                204 -> {
                    budgetList.deleteBudgetById(budgetId)
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
                    budgetList.deleteBudgetById(budgetId)
                    return HttpConstants.NOT_FOUND
                }
                else -> {
                    return HttpConstants.FAILED
                }
            }
        } catch (exception: Exception){
            return HttpConstants.FAILED
        }
    }

    suspend fun addBudget(name: String, budgetType: BudgetType, currencyCode: String?,
                          budgetAmount: String?, budgetPeriod: String?): ApiResponses<BudgetListSuccessModel>{
        return try {
            val networkCall = budgetService.addBudget(name, budgetType.toString(), currencyCode, budgetAmount, budgetPeriod)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }

    private suspend fun parseResponse(responseFromServer: Response<BudgetListSuccessModel>): ApiResponses<BudgetListSuccessModel> {
        val responseBody = responseFromServer.body()
        val responseErrorBody = responseFromServer.errorBody()
        if(responseBody != null && responseFromServer.isSuccessful){
            insertBudgetList(responseBody.data)
            return ApiResponses(response = responseBody)
        } else {
            if (responseErrorBody != null) {
                // Ignore lint warning. False positive
                // https://github.com/square/retrofit/issues/3255#issuecomment-557734546
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(responseErrorBody.source())
                val errorMessage = when {
                    moshi?.errors?.name != null -> moshi.errors.name[0]
                    else -> moshi?.message ?: "Error occurred while saving budget"
                }
                return ApiResponses(errorMessage = errorMessage)
            }
            return ApiResponses(errorMessage = "Error occurred while saving budget")
        }

    }

    suspend fun getSpentByBudgetName(budgetName: String, currencySymbol: String) =
            spentDao.getSpentAmountByBudgetName(budgetName, currencySymbol)

    suspend fun getUniqueCurrencySymbolInSpentByBudgetId(budgetId: Long) =
            budgetLimitDao.getUniqueCurrencySymbolInSpentByBudgetId(budgetId)

    suspend fun getBudgetLimitIdByNameAndCurrencyCodeAndDate(budgetName: String, currencySymbol: String,
                                                           startDate: String, endDate: String) =
            budgetLimitDao.getBudgetLimitIdByNameAndCurrencyCodeAndDate(budgetName, currencySymbol, startDate, endDate)
}