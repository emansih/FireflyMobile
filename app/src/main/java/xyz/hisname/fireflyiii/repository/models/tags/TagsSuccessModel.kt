package xyz.hisname.fireflyiii.repository.models.tags

import com.google.gson.annotations.SerializedName

data class TagsSuccessModel(
        @SerializedName("data")
        val data: TagsData
)