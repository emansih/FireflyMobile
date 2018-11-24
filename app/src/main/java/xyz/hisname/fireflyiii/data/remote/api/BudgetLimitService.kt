package xyz.hisname.fireflyiii.data.remote.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import xyz.hisname.fireflyiii.Constants.Companion.BUDGET_LIMIT_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.budget.limit.BudgetLimitModel

interface BudgetLimitService {

    @GET(BUDGET_LIMIT_API_ENDPOINT)
    fun getAllBudgetLimits(): Call<BudgetLimitModel>

    @GET(BUDGET_LIMIT_API_ENDPOINT)
    fun getPaginatedBudgetLimits(@Query("page") page: Int): Call<BudgetLimitModel>

}