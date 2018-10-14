package xyz.hisname.fireflyiii.repository.models.rules

import com.google.gson.annotations.SerializedName

data class Rulesmodel(
        @SerializedName("data")
        val data: List<RulesData>,
        val included: List<Included>,
        val meta: Meta,
        val links: Links
)