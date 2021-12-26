/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.ui.onboarding

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.account.NewAccountManager
import xyz.hisname.fireflyiii.data.local.account.OldAuthenticatorManager
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.dao.FireflyUserDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.data.remote.firefly.api.OAuthService
import xyz.hisname.fireflyiii.data.remote.firefly.api.SystemInfoService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.repository.models.FireflyUsers
import xyz.hisname.fireflyiii.repository.models.auth.AuthModel
import xyz.hisname.fireflyiii.repository.userinfo.SystemInfoRepository
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.isAscii
import java.io.File
import java.net.UnknownServiceException
import java.security.cert.CertificateException
import java.util.*

class AuthActivityViewModel(application: Application): BaseViewModel(application) {

    val isShowingBack: MutableLiveData<Boolean> = MutableLiveData()
    val baseUrl: MutableLiveData<String> = MutableLiveData()
    val isAuthenticated: MutableLiveData<Boolean> = MutableLiveData()
    val showInfoMessage: MutableLiveData<String> = MutableLiveData()
    val showErrorMessage: MutableLiveData<String> = MutableLiveData()

    private val applicationContext = getApplication<Application>()
    private val accountManager = AccountManager.get(applicationContext)
    private val customCaFile by lazy {
        File(getApplication<Application>().filesDir.path + "/" + getUniqueHash() + ".pem")
    }
    private val systemInfoRepository by lazy { SystemInfoRepository(
            genericService().create(SystemInfoService::class.java),
            sharedPref(), newManager())
    }
    private lateinit var repository: AccountRepository
    private lateinit var authenticatorManager: NewAccountManager

    fun authViaPat(baseUrl: String, accessToken: String, fileUri: Uri?) {
        if(accessToken.isEmpty()){
            showInfoMessage.postValue("Personal Access Token Required!")
            return
        }
        if(baseUrl.isEmpty()){
            showInfoMessage.postValue("Base URL Required!")
            return
        }
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            val accountHash = authInit(accessToken, baseUrl)
            val fireflyUserDao =  FireflyUserDatabase.getInstance(applicationContext).fireflyUserDao()
            val activeUserHash = fireflyUserDao.getUniqueHash()
            if(activeUserHash.isNotBlank()){
                fireflyUserDao.updateActiveUser(activeUserHash, false)
            }
            fireflyUserDao.insert(
                FireflyUsers(0, accountHash, "", baseUrl, true)
            )
            if(fileUri != null && fileUri.toString().isNotBlank()) {
                FileUtils.saveCaFile(fileUri, getApplication(), accountHash)
            }
        }

        val accountDao = AppDatabase.getInstance(applicationContext, getUniqueHash()).accountDataDao()
        val accountsService = genericService().create(AccountsService::class.java)
        repository = AccountRepository(accountDao, accountsService)
        viewModelScope.launch(Dispatchers.IO){
            try {
                repository.authViaPat()
                authenticatorManager.authMethod = "pat"
                isAuthenticated.postValue(true)
            } catch (exception: UnknownServiceException){
                FileUtils.deleteCaFile(customCaFile)
                showErrorMessage.postValue("http is not supported. Please use https")
                isAuthenticated.postValue(false)
            } catch (certificateException: CertificateException){
                FileUtils.deleteCaFile(customCaFile)
                showErrorMessage.postValue("Are you using self signed cert?")
                isAuthenticated.postValue(false)
            } catch (exception: Exception){
                FileUtils.deleteCaFile(customCaFile)
                showErrorMessage.postValue(exception.localizedMessage)
                isAuthenticated.postValue(false)
            }
            isLoading.postValue(false)
        }
    }


    fun authViaOauth(baseUrl: String, clientSecret: String, clientId: String, fileUri: Uri?): Boolean{
        if(baseUrl.isEmpty()){
            showInfoMessage.postValue("Base URL Required!")
            return false
        }
        if(clientSecret.isEmpty()){
            showInfoMessage.postValue("Client Secret Required!")
            return false
        }
        if(clientId.isEmpty()){
            showInfoMessage.postValue("Client ID Required!")
            return false
        }
        val accountHash = authInit("", baseUrl)
        if(fileUri != null && fileUri.toString().isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO){
                val fireflyUserDao =  FireflyUserDatabase.getInstance(applicationContext).fireflyUserDao()
                val activeUserHash = fireflyUserDao.getUniqueHash()
                if(activeUserHash.isNotBlank()){
                    fireflyUserDao.updateActiveUser(activeUserHash, false)
                }
                fireflyUserDao.insert(
                    FireflyUsers(0, accountHash, "", baseUrl, true)
                )
                if(fileUri.toString().isNotBlank()) {
                    FileUtils.saveCaFile(fileUri, getApplication(), accountHash)
                }
            }
        }
        authenticatorManager.clientId = clientId
        authenticatorManager.secretKey = clientSecret
        return true
    }

    fun getAccessToken(code: String, isDemo: Boolean = false, email: String, hostUrl : String){
        isLoading.postValue(true)
        if (!code.isAscii()) {
            // Issue #46 on Github
            // https://github.com/emansih/FireflyMobile/issues/46
            isAuthenticated.postValue(false)
            showErrorMessage.postValue("Bearer Token contains invalid Characters!")
        } else {
            var networkCall: Response<AuthModel>?
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val redirectUri = if(isDemo){
                        Constants.DEMO_REDIRECT_URI
                    } else {
                        Constants.REDIRECT_URI
                    }
                    val oAuthService = genericService().create(OAuthService::class.java)
                    networkCall = oAuthService.getAccessToken(code.trim(), authenticatorManager.clientId, authenticatorManager.secretKey, redirectUri)
                    val authResponse = networkCall?.body()
                    val errorBody = networkCall?.errorBody()
                    if (authResponse != null && networkCall?.isSuccessful != false) {
                        authenticatorManager.accessToken = authResponse.access_token.trim()
                        authenticatorManager.refreshToken = authResponse.refresh_token.trim()
                        authenticatorManager.tokenExpiry = authResponse.expires_in
                        authenticatorManager.authMethod = "oauth"
                        isAuthenticated.postValue(true)
                    } else if(errorBody != null){
                        FileUtils.deleteCaFile(customCaFile)
                        val errorBodyMessage = String(errorBody.bytes())
                        showErrorMessage.postValue(errorBodyMessage)
                        isAuthenticated.postValue(false)
                    }
                } catch (exception: UnknownServiceException){
                    FileUtils.deleteCaFile(customCaFile)
                    showErrorMessage.postValue("http is not supported. Please use https")
                    isAuthenticated.postValue(false)
                } catch (certificateException: CertificateException){
                    FileUtils.deleteCaFile(customCaFile)
                    showErrorMessage.postValue("Are you using self signed cert?")
                    isAuthenticated.postValue(false)
                } catch (exception: Exception){
                    FileUtils.deleteCaFile(customCaFile)
                    showErrorMessage.postValue(exception.localizedMessage)
                    isAuthenticated.postValue(false)
                }
            }
            isLoading.postValue(false)
        }
    }

    fun setDemo(code: String){
        val accountHash = authInit("", "https://demo.firefly-iii.org")
        authenticatorManager.clientId = "2"
        authenticatorManager.secretKey = "tfWoJQbmV88Fxej1ysAPIxFireflyIIIApiToken"
        viewModelScope.launch(Dispatchers.IO){
            val fireflyUserDao = FireflyUserDatabase.getInstance(applicationContext).fireflyUserDao()
            // This is now the default account. We unset the previous default(if it exists) and set demo as default
            fireflyUserDao.unsetDefaultUser()
            fireflyUserDao.insert(FireflyUsers(
                0, accountHash,  "demo@firefly", "https://demo.firefly-iii.org", true
            ))
        }
        getAccessToken(code, true, "demo@firefly", "https://demo.firefly-iii.org")
    }

    fun getUser(): LiveData<Boolean> {
        val apiOk: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            apiOk.postValue(false)
        }) {
            val url = FireflyUserDatabase
                .getInstance(applicationContext)
                .fireflyUserDao()
                .getCurrentActiveUserUrl()
            systemInfoRepository.getCurrentUserInfo(url,
                accountManager, FireflyUserDatabase.getInstance(applicationContext))
            apiOk.postValue(true)
        }
        return apiOk
    }

    fun userSystem(): LiveData<Boolean> {
        val apiOk: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            apiOk.postValue(false)
        }){
            systemInfoRepository.getUserSystem()
            apiOk.postValue(true)
        }
        return apiOk
    }

    private fun authInit(accessToken: String, baseUrl: String): String{
        val uuid = UUID.randomUUID().toString()
        FireflyClient.destroyInstance()
        authenticatorManager = NewAccountManager(accountManager, uuid)
        authenticatorManager.initializeAccount()
        authenticatorManager.accessToken = accessToken.trim()
        AppPref(sharedPref()).baseUrl = baseUrl
        return uuid
    }
}