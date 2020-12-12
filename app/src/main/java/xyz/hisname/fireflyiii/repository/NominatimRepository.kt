package xyz.hisname.fireflyiii.repository

import xyz.hisname.fireflyiii.data.remote.nominatim.NominatimClient
import xyz.hisname.fireflyiii.data.remote.nominatim.api.SearchService

class NominatimRepository {

    suspend fun getLocationFromQuery(location: String): List<String> {
        val client = NominatimClient.getClient()
        val locationResult = arrayListOf<String>()
        try {
            client.create(SearchService::class.java).searchLocation(location).forEach { search ->
                locationResult.add(search.display_name)
            }
        } catch (exception: Exception) { }
        return locationResult
    }
}