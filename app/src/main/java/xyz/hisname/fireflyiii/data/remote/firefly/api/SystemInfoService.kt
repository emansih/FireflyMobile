package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Response
import retrofit2.http.GET
import xyz.hisname.fireflyiii.Constants.Companion.SYSTEM_INFO_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.userinfo.system.SystemInfoModel
import xyz.hisname.fireflyiii.repository.models.userinfo.user.UserDataModel


interface SystemInfoService {

    @GET(SYSTEM_INFO_ENDPOINT)
    suspend fun getSystemInfo(): Response<SystemInfoModel>

    @GET("$SYSTEM_INFO_ENDPOINT/user")
    suspend fun getCurrentUserInfo(): Response<UserDataModel>

}