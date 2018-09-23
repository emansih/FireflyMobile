package xyz.hisname.fireflyiii.repository.models.userinfo.user

import com.google.gson.annotations.SerializedName

data class UserData(
        val type: String,
        val id: String,
        @SerializedName("attributes")
        val userAttributes: UserAttributes)