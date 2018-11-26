package xyz.hisname.fireflyiii.service

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.AccountManager.KEY_BOOLEAN_RESULT
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import xyz.hisname.fireflyiii.ui.onboarding.OnboardingActivity

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

    override fun addAccount(response: AccountAuthenticatorResponse, accountType: String,
                            authTokenType: String, requiredFeatures: Array<String>, options: Bundle): Bundle {
        val loginActivity = Intent(context, OnboardingActivity::class.java)
        return bundleOf(AccountManager.KEY_INTENT to loginActivity)
    }
}