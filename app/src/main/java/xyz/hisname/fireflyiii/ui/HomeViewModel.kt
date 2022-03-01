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

package xyz.hisname.fireflyiii.ui

import android.accounts.AccountManager
import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.account.NewAccountManager
import xyz.hisname.fireflyiii.data.local.account.OldAuthenticatorManager
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.dao.FireflyUserDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.bills.BillRepository
import xyz.hisname.fireflyiii.repository.bills.BillsPaidRepository
import xyz.hisname.fireflyiii.repository.models.FireflyUsers
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.workers.RefreshTokenWorker
import java.io.File
import java.util.*

class HomeViewModel(application: Application): BaseViewModel(application) {

    private val billsService by lazy { genericService().create(BillsService::class.java) }
    private val billDataDao by lazy {  AppDatabase.getInstance(application, getUniqueHash()).billDataDao() }
    private val billPaidDao by lazy {  AppDatabase.getInstance(application, getUniqueHash()).billPaidDao() }
    private val billPayDao by lazy {  AppDatabase.getInstance(application, getUniqueHash()).billPayDao() }
    private val billRepository by lazy { BillRepository(billDataDao, billsService) }
    private val billPaidRepository by lazy { BillsPaidRepository(billPaidDao, billsService) }
    private val fireflyUserDatabase by lazy { FireflyUserDatabase.getInstance(application).fireflyUserDao() }

    fun shouldGoToAuth(): Boolean{
        val application = getApplication<Application>()
        val oldDatabase = application.getDatabasePath(Constants.DB_NAME)
        return getActiveUserEmail() == null && !oldDatabase.exists()
    }

    fun updateActiveUser(userId: Long){
        viewModelScope.launch(Dispatchers.IO) {
            fireflyUserDatabase.removeActiveUser()
            fireflyUserDatabase.updateActiveUser(userId)
            FireflyClient.destroyInstance()
        }
    }

    fun getNoOfBillsDueToday(): LiveData<Int> {
        val count = MutableLiveData<Int>()
        viewModelScope.launch(Dispatchers.IO){
            val billDue = billRepository.getBillDueFromDate(DateTimeUtil.getTodayDate())
            val billPaidId = billPaidRepository.getBillPaidByDate(DateTimeUtil.getTodayDate(),
                    DateTimeUtil.getTodayDate(), billPayDao)
            val billDueId = arrayListOf<Long>()
            billDue.forEach {  billData ->
                billDueId.add(billData.billId)
            }
            val billIdDifference = billDueId.minus(billPaidId)
            count.postValue(billIdDifference.size)
        }
        return count
    }

    fun getFireflyUsers(): LiveData<List<FireflyUsers>>{
        val usersLiveData = MutableLiveData<List<FireflyUsers>>()
        viewModelScope.launch(Dispatchers.IO){
            removeInvalidFireflyAccounts()
            usersLiveData.postValue(fireflyUserDatabase.getAllUser())
        }
        return usersLiveData
    }

    private suspend fun removeInvalidFireflyAccounts() {
        fireflyUserDatabase.getAllUser()
            .filter { it.userEmail.isEmpty() }
            .forEach { removeFireflyAccount(it.uniqueHash) }
    }

    fun removeFireflyAccounts(fireflyUsers: List<FireflyUsers>){
        viewModelScope.launch(Dispatchers.IO){
            fireflyUsers.forEach { removeFireflyAccount(it.uniqueHash) }
        }
    }

    private fun removeFireflyAccount(uuid: String) {
        val app = getApplication<Application>()
        val accountManager = NewAccountManager(AccountManager.get(app), uuid)
        accountManager.destroyAccount().result
    }

    fun migrateFirefly(){
        /* Migration routine:
         * 1. Check if old database exists(firefly.db)
         *      - If it exists rename it to uniquehash-photuris.db
         *      - If it does not exist, user is a new user
         * 2. Add user to user account database
         * 3. Rename user's account
         * 4. Rename shared preference
         * 5. Rename custom CA file(if it exists)
         * 6. Cancel existing work manager work(s) and re-queue them and make them multi user aware(Only cancel refresh_token work)
         * 7. Rename tmp database
         */
        val application = getApplication<Application>()
        val oldDatabase = application.getDatabasePath(Constants.DB_NAME)
        val uniqueHash = UUID.randomUUID().toString()

        if (oldDatabase.exists()){
            val accManager = OldAuthenticatorManager(AccountManager.get(getApplication()))
            val authEmail = accManager.userEmail
            AppDatabase.destroyInstance()
            oldDatabase.renameTo(File(application.getDatabasePath("$uniqueHash-photuris.db").toString()))
            oldDatabase.delete()
            FireflyClient.destroyInstance()
            val oldSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication())
            val userHost = AppPref(oldSharedPref).baseUrl
            viewModelScope.launch(Dispatchers.IO){
                fireflyUserDatabase.insert(
                    FireflyUsers(
                        0L, uniqueHash, authEmail, userHost, true
                    )
                )
            }
            val accountSecretKey = accManager.secretKey
            val accountAccessToken = accManager.accessToken
            val accountClientId = accManager.clientId
            val accountTokenExpiry = accManager.tokenExpiry
            val accountRefreshToken = accManager.refreshToken
            val accountAuthMethod = accManager.authMethod
            accManager.destroyAccount()
            val newAccountManager = NewAccountManager(AccountManager.get(application), uniqueHash)
            newAccountManager.initializeAccount()
            newAccountManager.secretKey = accountSecretKey
            newAccountManager.accessToken = accountAccessToken
            newAccountManager.clientId = accountClientId
            newAccountManager.refreshToken = accountRefreshToken
            newAccountManager.authMethod = accountAuthMethod
            newAccountManager.tokenExpiry = accountTokenExpiry
            val fileArray = File(application.applicationInfo.dataDir + "/shared_prefs").listFiles()
            fileArray?.forEach {  file ->
                if(file.name.startsWith("xyz.hisname.fireflyiii") && file.name.endsWith("preferences.xml")){
                    file.renameTo(File(application.applicationInfo.dataDir + "/shared_prefs/" + uniqueHash + "-user-preferences.xml"))
                }
            }
            val refreshTokenPref = getApplication<Application>()
                .getSharedPreferences("$uniqueHash-user-preferences", Context.MODE_PRIVATE)
                .getBoolean("auto_refresh_token", false)
            val refreshTokenInterval = getApplication<Application>()
                .getSharedPreferences("$uniqueHash-user-preferences", Context.MODE_PRIVATE)
                .getLong("refresh_token_interval", 0)

            // User has enabled refreshing token preference
            if(refreshTokenPref && refreshTokenInterval > 0){
                RefreshTokenWorker.migrateWorkerV1ToV2(WorkManager.getInstance(getApplication()), uniqueHash, refreshTokenInterval)
            }
            val tmpDb = File("temp-" + Constants.DB_NAME)
            if(tmpDb.exists()){
                tmpDb.renameTo(File(application.getDatabasePath("$uniqueHash-temp-" + Constants.DB_NAME).toString()))
            }
        }

        val customCaFile = File(application.applicationInfo.dataDir + "/user_custom.pem")
        if(customCaFile.exists()){
            customCaFile.renameTo(File(application.applicationInfo.dataDir + "/" + uniqueHash + ".pem"))
            customCaFile.delete()
        }
    }

    fun deleteUnusedAccount(){
        viewModelScope.launch(Dispatchers.IO){
            val accountArray = arrayListOf<String>()
            AccountManager.get(getApplication()).accounts.forEach { account ->
                accountArray.add(account.name)
            }

            val userArray = arrayListOf<String>()
            fireflyUserDatabase.getAllUser().forEach { fireflyUsers ->
                userArray.add(fireflyUsers.uniqueHash)
            }
            val accountToRemove = accountArray.filterNot { userArray.contains(it) }
            accountToRemove.forEach { removeFireflyAccount(it) }
        }
    }

}