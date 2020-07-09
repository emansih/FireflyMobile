package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import xyz.hisname.fireflyiii.Constants.Companion.AVAILABLE_BUDGET_API_ENDPOINT
import xyz.hisname.fireflyiii.Constants.Companion.BUDGET_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.budget.BudgetModel
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetListModel

interface BudgetService {

    @GET(AVAILABLE_BUDGET_API_ENDPOINT)
    fun getAllBudget(): Call<BudgetModel>

    @GET(AVAILABLE_BUDGET_API_ENDPOINT)
    fun getPaginatedBudget(@Query("page") page: Int): Call<BudgetModel>

    @GET(BUDGET_API_ENDPOINT)
    suspend fun getPaginatedSpentBudget(@Query("page") page: Int, @Query("start") start: String,
                                @Query("end") end: String): Response<BudgetListModel>

    @GET(BUDGET_API_ENDPOINT)
    suspend fun getPaginatedSpentBudget(@Query("page") page: Int): Response<BudgetListModel>

}