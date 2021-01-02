package xyz.hisname.fireflyiii.ui.onboarding

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
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.data.remote.firefly.api.OAuthService
import xyz.hisname.fireflyiii.data.remote.firefly.api.SystemInfoService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.repository.models.auth.AuthModel
import xyz.hisname.fireflyiii.repository.userinfo.SystemInfoRepository
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.isAscii
import java.io.File
import java.net.UnknownServiceException
import java.security.cert.CertificateException

class AuthActivityViewModel(application: Application): BaseViewModel(application) {

    val isShowingBack: MutableLiveData<Boolean> = MutableLiveData()
    val baseUrl: MutableLiveData<String> = MutableLiveData()
    val isAuthenticated: MutableLiveData<Boolean> = MutableLiveData()
    val showInfoMessage: MutableLiveData<String> = MutableLiveData()
    val showErrorMessage: MutableLiveData<String> = MutableLiveData()

    private val applicationContext = getApplication<Application>()
    private val accountManager = AccountManager.get(applicationContext)
    private val customCaFile by lazy { File(applicationContext.filesDir.toString() + File.pathSeparator +  "user_custom.pem") }
    private val systemInfoRepository by lazy { SystemInfoRepository(
            genericService().create(SystemInfoService::class.java),
            sharedPref,
            accManager)
    }
    private lateinit var repository: AccountRepository


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
        if(fileUri != null && fileUri.toString().isNotBlank()) {
            FileUtils.saveCaFile(fileUri, getApplication())
        }
        authInit(accessToken, baseUrl)
        val accountDao = AppDatabase.getInstance(applicationContext).accountDataDao()
        val accountsService = genericService().create(AccountsService::class.java)
        repository = AccountRepository(accountDao, accountsService)
        viewModelScope.launch(Dispatchers.IO){
            try {
                repository.authViaPat()
                AuthenticatorManager(accountManager).authMethod = "pat"
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
        if(fileUri != null && fileUri.toString().isNotBlank()) {
            FileUtils.saveCaFile(fileUri, getApplication())
        }
        authInit("", baseUrl)
        accManager.clientId = clientId
        accManager.secretKey = clientSecret
        return true
    }

    fun getAccessToken(code: String, isDemo: Boolean = false){
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
                    networkCall = oAuthService.getAccessToken(code.trim(), accManager.clientId,
                            accManager.secretKey, redirectUri)
                    val authResponse = networkCall?.body()
                    val errorBody = networkCall?.errorBody()
                    if (authResponse != null && networkCall?.isSuccessful != false) {
                        accManager.accessToken = authResponse.access_token.trim()
                        accManager.refreshToken = authResponse.refresh_token.trim()
                        accManager.tokenExpiry = authResponse.expires_in
                        accManager.authMethod = "oauth"
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
        authInit("", "https://demo.firefly-iii.org")
        accManager.clientId = "2"
        accManager.secretKey = "tfWoJQbmV88Fxej1ysAPIxFireflyIIIApiToken"
        getAccessToken(code, true)
    }

    fun getUser(): LiveData<Boolean> {
        val apiOk: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            apiOk.postValue(false)
        }) {
            systemInfoRepository.getCurrentUserInfo()
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

    private fun authInit(accessToken: String, baseUrl: String){
        FireflyClient.destroyInstance()
        accManager.destroyAccount()
        AuthenticatorManager(accountManager).initializeAccount()
        AuthenticatorManager(accountManager).accessToken = accessToken.trim()
        AppPref(sharedPref).baseUrl = baseUrl
    }
}