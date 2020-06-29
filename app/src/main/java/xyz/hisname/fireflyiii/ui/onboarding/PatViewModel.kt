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
import java.io.File

class PatViewModel(application: Application): BaseViewModel(application) {

    private val applicationContext = getApplication<Application>()
    private val accountManager by lazy { AccountManager.get(applicationContext) }
    private val accountDao by lazy { AppDatabase.getInstance(applicationContext).accountDataDao() }
    private val accountsService by lazy { genericService()?.create(AccountsService::class.java)  }
    private val repository by lazy { AccountRepository(accountDao, accountsService) }
    fun getFilePath(fileUri: Uri?) = FileUtils.getPathFromUri(applicationContext, fileUri ?: Uri.EMPTY)

    fun authenticate(fileUri: Uri?, accessToken: String, baseUrl: String): LiveData<Boolean> {
        if(fileUri != null && fileUri.toString().isNotBlank()) {
            FileUtils.copyFile(File(FileUtils.getPathFromUri(applicationContext, fileUri)),
                    File(applicationContext.filesDir.path + "/user_custom.pem"))
            val filePath = FileUtils.getPathFromUri(getApplication(), fileUri)
            val file = FileUtils.readFileContent(File(filePath))
            if (file.isBlank()) {
                apiResponse.postValue("Certificate file is empty. Continuing anyway...")
            }
        }
        authInit(accessToken, baseUrl)
        viewModelScope.launch(Dispatchers.IO){
            repository.authViaPat()
        }.invokeOnCompletion {
            apiResponse.postValue(repository.responseApi.value)
            if(repository.authStatus.value == true){
                AuthenticatorManager(accountManager).authMethod = "pat"
            }
        }
        return repository.authStatus
    }

    private fun authInit(accessToken: String, baseUrl: String){
        FireflyClient.destroyInstance()
        AuthenticatorManager(accountManager).destroyAccount()
        AuthenticatorManager(accountManager).initializeAccount()
        AuthenticatorManager(accountManager).accessToken = accessToken.trim()
        AppPref(sharedPref).baseUrl = baseUrl
    }

}