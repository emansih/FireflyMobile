package xyz.hisname.fireflyiii.ui.onboarding

import android.accounts.AccountManager
import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.util.FileUtils

class PatViewModel(application: Application): BaseViewModel(application) {

    private val applicationContext = getApplication<Application>()
    private val accountManager by lazy { AccountManager.get(applicationContext) }
    private val accountDao by lazy { AppDatabase.getInstance(applicationContext).accountDataDao() }
    private var accountsService: AccountsService? = null
    private lateinit var repository: AccountRepository

    fun getFilePath(fileUri: Uri?) = FileUtils.getPathFromUri(applicationContext, fileUri ?: Uri.EMPTY)

    fun authenticate(fileUri: Uri?, accessToken: String, baseUrl: String): LiveData<String> {
        if(fileUri != null && fileUri.toString().isNotBlank()) {
            FileUtils.saveCaFile(fileUri, applicationContext)
        }
        authInit(accessToken, baseUrl)
        repository = AccountRepository(accountDao, accountsService)
        viewModelScope.launch(Dispatchers.IO){
            repository.authViaPat()
        }.invokeOnCompletion {
            if(repository.authStatus.value == true){
                AuthenticatorManager(accountManager).authMethod = "pat"
            }
        }
        return repository.responseApi
    }

    private fun authInit(accessToken: String, baseUrl: String){
        FireflyClient.destroyInstance()
        accountsService = null
        AuthenticatorManager(accountManager).destroyAccount()
        AuthenticatorManager(accountManager).initializeAccount()
        AuthenticatorManager(accountManager).accessToken = accessToken.trim()
        AppPref(sharedPref).baseUrl = baseUrl
        accountsService = genericService()?.create(AccountsService::class.java)
    }

}