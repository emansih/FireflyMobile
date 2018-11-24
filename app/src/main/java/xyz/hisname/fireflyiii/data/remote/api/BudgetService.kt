package xyz.hisname.fireflyiii.data.remote.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import xyz.hisname.fireflyiii.Constants.Companion.BUDGET_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.budget.budget.BudgetModel

interface BudgetService {

    @GET(BUDGET_API_ENDPOINT)
    fun getAllBudget(): Call<BudgetModel>

    @GET(BUDGET_API_ENDPOINT)
    fun getPaginatedBudget(@Query("page") page: Int): Call<BudgetModel>

}