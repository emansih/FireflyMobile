package xyz.hisname.fireflyiii.repository.api

import retrofit2.Call
import retrofit2.http.GET
import xyz.hisname.fireflyiii.Constants.Companion.SYSTEM_INFO_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.userinfo.system.SystemInfoModel
import xyz.hisname.fireflyiii.repository.models.userinfo.user.UserDataModel


// Link to relevant docs: https://firefly-iii.readthedocs.io/en/latest/api/about.html
interface SystemInfoService {

    @GET(SYSTEM_INFO_ENDPOINT)
    fun getSystemInfo(): Call<SystemInfoModel>

    @GET("$SYSTEM_INFO_ENDPOINT/user")
    fun getCurrentUserInfo(): Call<UserDataModel>

}