package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import xyz.hisname.fireflyiii.Constants.Companion.AVAILABLE_BUDGET_API_ENDPOINT
import xyz.hisname.fireflyiii.Constants.Companion.BUDGET_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.budget.BudgetModel
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListModel
import xyz.hisname.fireflyiii.repository.models.budget.limits.BudgetLimitModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionModel

interface BudgetService {

    @GET(AVAILABLE_BUDGET_API_ENDPOINT)
    fun getAllBudget(): Call<BudgetModel>

    @GET(AVAILABLE_BUDGET_API_ENDPOINT)
    fun getPaginatedBudget(@Query("page") page: Int): Call<BudgetModel>

    @GET(AVAILABLE_BUDGET_API_ENDPOINT)
    suspend fun getAvailableBudget(@Query("page") page: Int,
                                   @Query("start") start: String,
                                   @Query("end") end: String): Response<BudgetModel>

    @GET(BUDGET_API_ENDPOINT)
    suspend fun getPaginatedSpentBudget(@Query("page") page: Int, @Query("start") start: String,
                                @Query("end") end: String): Response<BudgetListModel>

    @GET(BUDGET_API_ENDPOINT)
    suspend fun getPaginatedSpentBudget(@Query("page") page: Int): Response<BudgetListModel>

    @GET("$BUDGET_API_ENDPOINT/{id}/limits")
    suspend fun getBudgetLimit(@Path("id") budgetId: Long, @Query("start") start: String,
                               @Query("end") end: String): Response<BudgetLimitModel>

    @GET("$BUDGET_API_ENDPOINT/{id}/transactions")
    suspend fun getPaginatedTransactionByBudget(
            @Path("id") budgetId: Long,
            @Query("page") page: Int,
            @Query("start") start: String,
            @Query("end") end: String,
            @Query("type") transactionType: String): Response<TransactionModel>


}