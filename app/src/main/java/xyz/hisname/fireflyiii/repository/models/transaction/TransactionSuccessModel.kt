package xyz.hisname.fireflyiii.repository.models.transaction

import com.google.gson.annotations.SerializedName

data class TransactionSuccessModel(
        @SerializedName("data")
        val data: List<TransactionData>
)