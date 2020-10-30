package xyz.hisname.fireflyiii.data.remote.firefly.api

import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import xyz.hisname.fireflyiii.Constants.Companion.SUMMARY_API_ENDPOINT

interface SummaryService {

    @GET(SUMMARY_API_ENDPOINT)
    fun getBasicSummary(@Query("start") startDate: String,
                        @Query("end") endDate: String,
                        @Query("currency_code") currency: String): Call<JSONObject>
}