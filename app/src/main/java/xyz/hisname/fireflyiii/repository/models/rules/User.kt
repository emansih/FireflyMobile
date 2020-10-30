package xyz.hisname.fireflyiii.repository.models.rules

import com.squareup.moshi.Json

data class User(
        val links: Links,
        @Json(name ="data")
        val data: RulesData
)