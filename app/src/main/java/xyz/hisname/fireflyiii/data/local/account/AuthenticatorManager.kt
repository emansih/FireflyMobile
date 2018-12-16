package xyz.hisname.fireflyiii.data.local.account

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Build
import androidx.core.os.bundleOf
import java.util.concurrent.TimeUnit

class AuthenticatorManager(private val accountManager: AccountManager): AccountHelper {

    private val account by lazy { Account("Firefly III Mobile", "OAUTH") }

    override var secretKey
        get() = accountManager.getUserData(account, "SECRET_KEY") ?: ""
        set(value) {
            accountManager.setUserData(account, "SECRET_KEY", value)
        }

    override var accessToken
        get() = accountManager.getUserData(account, "ACCESS_TOKEN") ?: ""
        set(value) {
            accountManager.setUserData(account, "ACCESS_TOKEN", value)
        }

    override var clientId
        get() = accountManager.getUserData(account, "CLIENT_ID") ?: ""
        set(value) {
            accountManager.setUserData(account, "CLIENT_ID", value)
        }


    override var refreshToken
        get() = accountManager.getUserData(account, "REFRESH_TOKEN") ?: ""
        set(value) {
            accountManager.setUserData(account, "REFRESH_TOKEN", value)
        }

    override var authMethod
        get() = accountManager.getUserData(account, "AUTH_METHOD") ?: ""
        set(value) = accountManager.setUserData(account, "AUTH_METHOD", value)

    override fun destroyAccount() {
        val accountList = accountManager.getAccountsByType("OAUTH")
        for(items in accountList){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                accountManager.removeAccount(items, null, null, null)
            } else {
                accountManager.removeAccount(items, null, null)
            }
        }
    }

    override var tokenExpiry
        get() = accountManager.getUserData(account, "token_expires_in").toLong()
        set(value) {
            accountManager.setUserData(account,"token_expires_in", (System.currentTimeMillis() +
                    TimeUnit.MINUTES.toMillis(value)).toString())
        }

    override var userEmail
        get() = accountManager.getUserData(account, "USER_EMAIL") ?: ""
        set(value) {
            accountManager.setUserData(account, "USER_EMAIL", value)
        }

    override fun initializeAccount() {
        accountManager.addAccountExplicitly(account, "", bundleOf())
    }

    override fun isTokenValid(): Boolean{
        return System.currentTimeMillis() >= tokenExpiry
    }

}