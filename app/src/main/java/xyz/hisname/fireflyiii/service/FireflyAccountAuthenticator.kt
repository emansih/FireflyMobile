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

package xyz.hisname.fireflyiii.service

import android.accounts.*
import android.accounts.AccountManager.KEY_BOOLEAN_RESULT
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.FireflyUserDatabase
import xyz.hisname.fireflyiii.repository.models.FireflyUsers
import xyz.hisname.fireflyiii.ui.onboarding.AuthActivity
import java.io.File

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

    override fun addAccount(response: AccountAuthenticatorResponse, accountType: String, authTokenType: String?,
                            requiredFeatures: Array<String>?, options: Bundle?): Bundle {
        return bundleOf(AccountManager.KEY_INTENT to  Intent(context, AuthActivity::class.java))
    }

    override fun getAccountRemovalAllowed(response: AccountAuthenticatorResponse, account: Account): Bundle {
        runBlocking(Dispatchers.IO){
            context.deleteDatabase(account.name + "-photuris.db")
            context.deleteDatabase(account.name + "-temp-" + Constants.DB_NAME + "-photuris.db")
            context.deleteFile(account.name + ".pem")
            val sharedPrefDir = File(context.applicationInfo.dataDir + "/shared_prefs")
            sharedPrefDir.resolve(account.name + "-user-preferences.xml").delete()

            val fireflyUserDao = FireflyUserDatabase.getInstance(context).fireflyUserDao()
            val userToBeDeleted = fireflyUserDao.getUserByHash(account.name)
            if (userToBeDeleted != null) {
                fireflyUserDao.deleteUserByPrimaryKey(userToBeDeleted.id)

                val fireflyUsers = fireflyUserDao.getAllUser()
                if(userToBeDeleted.activeUser && fireflyUsers.isNotEmpty()){
                    fireflyUserDao.updateActiveUser(fireflyUsers[0].uniqueHash, true)
                }
            }

            WorkManager.getInstance(context).cancelAllWorkByTag(account.name)
        }
        return super.getAccountRemovalAllowed(response, account)
    }
}