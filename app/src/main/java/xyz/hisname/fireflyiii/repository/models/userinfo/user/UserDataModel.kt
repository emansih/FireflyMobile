package xyz.hisname.fireflyiii.repository.models.userinfo.user

import com.google.gson.annotations.SerializedName

data class UserDataModel(
        @SerializedName("data")
        val userData: UserData
)