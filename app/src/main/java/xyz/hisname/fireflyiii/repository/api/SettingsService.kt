package xyz.hisname.fireflyiii.repository.api

import retrofit2.Call
import retrofit2.http.GET
import xyz.hisname.fireflyiii.Constants.Companion.SETTINGS_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.userinfo.settings.SettingsModel

interface SettingsService {

    @GET("$SETTINGS_API_ENDPOINT/")
    fun getSettings(): Call<SettingsModel>

}