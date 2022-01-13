package xyz.hisname.fireflyiii.service

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViewsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import xyz.hisname.fireflyiii.data.local.account.NewAccountManager
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.dao.FireflyUserDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.ui.widgets.AccountListViewsFactory
import xyz.hisname.fireflyiii.util.getUniqueHash
import xyz.hisname.fireflyiii.util.network.CustomCa
import java.io.File

class AccountListWidgetService: RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val accountRepository = AccountRepository(
            AppDatabase.getInstance(application, getUniqueHash()).accountDataDao(),
            genericService().create(AccountsService::class.java)
        )
        val sharedPref = application.getSharedPreferences(
            application.getUniqueHash() + "-user-preferences", Context.MODE_PRIVATE)
        val appPref = AppPref(sharedPref)
        val accountDataList = arrayListOf<AccountData>()
        runBlocking(Dispatchers.IO){
            accountDataList.addAll(accountRepository.getAccountByType(appPref.accountListHomeScreenWidget))
        }
        return AccountListViewsFactory(accountDataList, application)
    }

    private fun genericService(): Retrofit {
        val cert = AppPref(sharedPref()).certValue
        val certFile = File(application.filesDir.path + "/" + getUniqueHash() + ".pem")
        return if (certFile.exists()) {
            val customCa = CustomCa(certFile)
            FireflyClient.getClient(getActiveUserUrl(), newManager().accessToken, cert,
                customCa.getCustomTrust(), customCa.getCustomSSL())
        } else {
            FireflyClient.getClient(getActiveUserUrl(), newManager().accessToken, cert, null, null)
        }
    }

    private fun getActiveUserUrl(): String {
        val activeUrl: String
        runBlocking(Dispatchers.IO){
            activeUrl = FireflyUserDatabase.getInstance(application).fireflyUserDao().getCurrentActiveUserUrl()
        }
        return activeUrl
    }

    private fun newManager(): NewAccountManager {
        return NewAccountManager(AccountManager.get(application), getUniqueHash())
    }

    private fun sharedPref() = application.getSharedPreferences(
            getUniqueHash() + "-user-preferences", Context.MODE_PRIVATE)
}