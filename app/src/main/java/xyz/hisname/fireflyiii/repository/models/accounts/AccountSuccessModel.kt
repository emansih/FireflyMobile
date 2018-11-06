package xyz.hisname.fireflyiii.repository.models.accounts

import com.google.gson.annotations.SerializedName
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData

data class AccountSuccessModel(
        @SerializedName("data")
        val data: AccountData
)