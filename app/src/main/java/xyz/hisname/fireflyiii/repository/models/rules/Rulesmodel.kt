package xyz.hisname.fireflyiii.repository.models.rules

import com.squareup.moshi.Json

data class Rulesmodel(
        @Json(name ="data")
        val data: List<RulesData>,
        val included: List<Included>,
        val meta: Meta,
        val links: Links
)