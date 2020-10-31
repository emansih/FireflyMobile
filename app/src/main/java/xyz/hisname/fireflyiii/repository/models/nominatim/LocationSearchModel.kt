package xyz.hisname.fireflyiii.repository.models.nominatim

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LocationSearchModel(
        val boundingbox: List<String>,
        val category: String,
        val display_name: String,
        val icon: String? = null,
        val importance: Double,
        val lat: Double,
        val licence: String,
        val lon: Double,
        val osm_id: Long,
        val osm_type: String,
        val place_id: Int,
        val place_rank: Int,
        val type: String
)