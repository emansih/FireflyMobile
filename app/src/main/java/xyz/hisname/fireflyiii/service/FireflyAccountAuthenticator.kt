package xyz.hisname.fireflyiii.service

import android.accounts.*
import android.accounts.AccountManager.KEY_BOOLEAN_RESULT
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import xyz.hisname.fireflyiii.ui.HomeActivity
import xyz.hisname.fireflyiii.ui.onboarding.AuthActivity

class FireflyAccountAuthenticator(private val context: Context): AbstractAccountAuthenticator(context) {

    override fun getAuthTokenLabel(authTokenType: String) = authTokenType


    override fun confirmCredentials(response: AccountAuthenticatorResponse, account: Account,
                                    options: Bundle) = null


    override fun updateCredentials(response: AccountAuthenticatorResponse, account: Account,
                                   authTokenType: String, options: Bundle) = null

    override fun getAuthToken(response: AccountAuthenticatorResponse, account: Account,
                              authTokenType: String, options: Bundle): Bundle {
        val accountManager = AccountManager.get(context)
        val authToken = accountManager.peekAuthToken(account, authTokenType)
        return bundleOf(AccountManager.KEY_ACCOUNT_NAME to account.name,
                AccountManager.KEY_ACCOUNT_TYPE to account.type, AccountManager.KEY_AUTHTOKEN to authToken)
    }

    override fun hasFeatures(response: AccountAuthenticatorResponse, account: Account,
                             features: Array<String>) = bundleOf(KEY_BOOLEAN_RESULT to false)


    override fun editProperties(response: AccountAuthenticatorResponse, accountType: String) = null

    @Throws(NetworkErrorException::class)
    override fun addAccount(response: AccountAuthenticatorResponse, accountType: String, authTokenType: String?,
                            requiredFeatures: Array<String>?, options: Bundle?): Bundle? {
        val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
        val accounts = accountManager.getAccountsByType("OAUTH")
        lateinit var activityToLaunch: Intent
        activityToLaunch = if(accounts.isEmpty()){
            Intent(context, AuthActivity::class.java)
        } else {
            Intent(context, HomeActivity::class.java)
        }
        return bundleOf(AccountManager.KEY_INTENT to activityToLaunch)
    }
}