package xyz.hisname.fireflyiii.repository.models.category

import com.google.gson.annotations.SerializedName

data class CategorySuccessModel(
        @SerializedName("data")
        val data: CategoryData
)