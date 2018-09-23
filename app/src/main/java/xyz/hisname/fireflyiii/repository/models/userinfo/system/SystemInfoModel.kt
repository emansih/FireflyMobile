package xyz.hisname.fireflyiii.repository.models.userinfo.system

import com.google.gson.annotations.SerializedName

data class SystemInfoModel(
        @SerializedName("data")
        val systemData: SystemData
)