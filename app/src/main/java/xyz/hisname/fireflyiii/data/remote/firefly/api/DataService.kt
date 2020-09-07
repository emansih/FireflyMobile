package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Query
import xyz.hisname.fireflyiii.Constants

interface DataService {

    @DELETE(Constants.DATA_API_ENDPOINT)
    suspend fun destroyItem(@Query("objects") item: String): Response<Void>

}