package xyz.hisname.fireflyiii.repository.models.userinfo.settings

import com.google.gson.annotations.SerializedName

data class SettingsData(
        val type: String,
        val id: String,
        @SerializedName("attributes")
        val attributes: SetttingsAttributes)