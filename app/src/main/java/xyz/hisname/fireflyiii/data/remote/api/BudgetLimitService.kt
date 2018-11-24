package xyz.hisname.fireflyiii.data.remote.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import xyz.hisname.fireflyiii.Constants.Companion.AVAILABLE_BUDGET_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.budget.limit.BudgetLimitModel

interface BudgetLimitService {

    @GET(AVAILABLE_BUDGET_API_ENDPOINT)
    fun getAllBudgetLimits(): Call<BudgetLimitModel>

    @GET(AVAILABLE_BUDGET_API_ENDPOINT)
    fun getPaginatedBudgetLimits(@Query("page") page: Int): Call<BudgetLimitModel>

}