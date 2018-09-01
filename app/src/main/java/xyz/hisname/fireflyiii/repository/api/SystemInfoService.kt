package xyz.hisname.fireflyiii.repository.api

import retrofit2.http.GET
import xyz.hisname.fireflyiii.Constants.Companion.SYSTEM_INFO_ENDPOINT


// Link to relevant docs: https://firefly-iii.readthedocs.io/en/latest/api/about.html
interface SystemInfoService {

    // TODO: add callbacks

    @GET(SYSTEM_INFO_ENDPOINT)
    fun getSystemInfo()

    @GET("$SYSTEM_INFO_ENDPOINT/user")
    fun getCurrentUserInfo()

}