package xyz.hisname.fireflyiii.data.local.account

import android.accounts.Account
import android.accounts.AccountManager
import androidx.core.os.bundleOf
import java.util.concurrent.TimeUnit

class NewAccountManager (private val accountManager: AccountManager,
                         private val accountHash: String) {

    private val account by lazy { Account(accountHash, "hisname.xyz") }

    var secretKey
        get() = accountManager.getUserData(account, "SECRET_KEY") ?: ""
        set(value) {
            accountManager.setUserData(account, "SECRET_KEY", value)
        }

    var accessToken
        get() = accountManager.getUserData(account, "ACCESS_TOKEN") ?: ""
        set(value) {
            accountManager.setUserData(account, "ACCESS_TOKEN", value)
        }

    var clientId
        get() = accountManager.getUserData(account, "CLIENT_ID") ?: ""
        set(value) {
            accountManager.setUserData(account, "CLIENT_ID", value)
        }


    var refreshToken
        get() = accountManager.getUserData(account, "REFRESH_TOKEN") ?: ""
        set(value) {
            accountManager.setUserData(account, "REFRESH_TOKEN", value)
        }

    var authMethod
        get() = accountManager.getUserData(account, "AUTH_METHOD") ?: ""
        set(value) = accountManager.setUserData(account, "AUTH_METHOD", value)

    fun destroyAccount() = accountManager.removeAccount(account, null, null, null)

    var tokenExpiry
        get() = accountManager.getUserData(account, "token_expires_in").toLong()
        set(value) {
            accountManager.setUserData(account,"token_expires_in", (System.currentTimeMillis() +
                    TimeUnit.MINUTES.toMillis(value)).toString())
        }

    fun initializeAccount() {
        accountManager.addAccountExplicitly(account, "", bundleOf())
    }

    fun isTokenValid(): Boolean{
        return System.currentTimeMillis() >= tokenExpiry
    }

}