package xyz.hisname.fireflyiii.repository.models.rules

import com.google.gson.annotations.SerializedName

data class User(
        val links: Links,
        @SerializedName("data")
        val data: RulesData
)