package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import xyz.hisname.fireflyiii.Constants.Companion.RECURRENCE_API_ENDPOINT

// Link to relevant doc: https://firefly-iii.readthedocs.io/en/latest/api/recurrences.html
interface RecurrenceService {

    // TODO: add callbacks

    @GET(RECURRENCE_API_ENDPOINT)
    fun getRecurrence()

    @DELETE("$RECURRENCE_API_ENDPOINT/{id}")
    fun deleteRecurrenceById(@Path("id") id: String)

    // TODO add function to add recurrence

    @GET("$RECURRENCE_API_ENDPOINT/{id}")
    fun getRecurrenceById(@Path("id") id: String)


}