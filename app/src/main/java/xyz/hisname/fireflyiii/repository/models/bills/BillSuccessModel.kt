package xyz.hisname.fireflyiii.repository.models.bills

import com.google.gson.annotations.SerializedName

data class BillSuccessModel(
        @SerializedName("data")
        val data: BillData
)