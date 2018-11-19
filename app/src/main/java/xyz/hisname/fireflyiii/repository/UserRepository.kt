package xyz.hisname.fireflyiii.repository

import xyz.hisname.fireflyiii.data.local.pref.AppPref

class UserRepository(appPref: AppPref) {

    val baseUrl = appPref.getBaseUrl()
    val accessToken = appPref.getAccessToken()
}