package xyz.hisname.fireflyiii.repository.models.piggy

import com.google.gson.annotations.SerializedName

data class PiggySuccessModel(
        @SerializedName("data")
        val data: PiggyData
)