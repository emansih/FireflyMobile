package xyz.hisname.fireflyiii.data.remote.nominatim.api

import retrofit2.http.POST
import retrofit2.http.Query
import xyz.hisname.fireflyiii.repository.models.nominatim.LocationSearchModel

interface SearchService {

    @POST("/search")
    suspend fun searchLocation(@Query("q")location: String,
                       @Query("format")outputFormat: String = "jsonv2"): List<LocationSearchModel>
}