package xyz.hisname.fireflyiii.data.remote.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.repository.models.summary.SummaryModel

interface SummaryService {

    @GET(Constants.SUMMARY_API_ENDPOINT)
    fun getSummaryData(@Query("start") startDate: String, @Query("end") endDate: String): Call<SummaryModel>
}