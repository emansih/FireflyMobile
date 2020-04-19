package xyz.hisname.fireflyiii.repository

import android.accounts.AccountManager
import android.app.Application
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import retrofit2.Retrofit
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.network.CustomCa
import java.io.File
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

open class BaseViewModel(application: Application) : AndroidViewModel(application){

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val apiResponse: MutableLiveData<String> = MutableLiveData()
    protected val accManager by lazy { AuthenticatorManager(AccountManager.get(getApplication()))  }
    protected val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(getApplication()) }
    private val customCa by lazy { CustomCa(("file://" + getApplication<Application>().filesDir.path + "/user_custom.pem").toUri().toFile()) }
    private val sslSocketFactory by lazy { customCa.getCustomSSL() }
    private val trustManager by lazy { customCa.getCustomTrust() }

    protected fun genericService(): Retrofit? {
        var cert = ""
        if (AppPref(sharedPref).enableCertPinning) {
            cert = AppPref(sharedPref).certValue
        }
        return if (AppPref(sharedPref).isCustomCa) {
            FireflyClient.getClient(AppPref(sharedPref).baseUrl,
                    accManager.accessToken, cert, trustManager, sslSocketFactory)
        } else {
            FireflyClient.getClient(AppPref(sharedPref).baseUrl,
                    accManager.accessToken, cert, null, null)
        }

    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}