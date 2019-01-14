package xyz.hisname.fireflyiii.data.remote.api

import retrofit2.Call
import retrofit2.http.GET
import xyz.hisname.fireflyiii.Constants.Companion.RULES_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.rules.Rulesmodel

interface RulesService {

    @GET("$RULES_API_ENDPOINT/")
    fun getAllRules(): Call<Rulesmodel>
}