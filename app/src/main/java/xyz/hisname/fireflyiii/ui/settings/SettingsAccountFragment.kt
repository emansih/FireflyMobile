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

package xyz.hisname.fireflyiii.ui.settings

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AlertDialog
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.repository.auth.AuthViewModel
import xyz.hisname.fireflyiii.ui.onboarding.AuthActivity
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.workers.RefreshTokenWorker
import java.util.*
import java.util.concurrent.TimeUnit


// TODO: Remove explicit type arguments. Broken on pref(1.1.0-alpha05)
class SettingsAccountFragment: BaseSettings() {

    private val accManager by lazy { AuthenticatorManager(AccountManager.get(requireContext())) }
    private val authMethodPref by lazy { accManager.authMethod }
    private val authViewModel by lazy { getImprovedViewModel(AuthViewModel::class.java) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_account_settings)
        setAccountSection()
    }

    private fun setAccountSection(){
        val fireflyUrlPref = findPreference<EditTextPreference>("fireflyUrl") as EditTextPreference
        fireflyUrlPref.apply {
            title = "Firefly URL"
            summary = AppPref(sharedPref).baseUrl
        }

        val accessTokenPref = findPreference<EditTextPreference>("access_token") as EditTextPreference
        accessTokenPref.apply {
            title = "Access Token"
            summary = accManager.secretKey
        }
        val authMethod = findPreference<Preference>("auth_method") as Preference

        if(Objects.equals(authMethodPref, "oauth")){
            authMethod.summary = "Open Authentication"
        } else {
            authMethod.summary = resources.getString(R.string.personal_access_token)
        }

        val tokenValidity = findPreference<Preference>("auth_token_time") as Preference
        tokenValidity.summary = DateTimeUtil.convertEpochToHumanTime(accManager.tokenExpiry)

        fireflyUrlPref.setOnPreferenceChangeListener { preference, newValue  ->
            preference.summary = newValue.toString()
            FireflyClient.destroyInstance()
            true
        }
        accessTokenPref.setOnPreferenceChangeListener { preference, newValue  ->
            preference.summary = newValue.toString()
            FireflyClient.destroyInstance()
            true
        }
        val refreshToken = findPreference<Preference>("refresh_token") as Preference
        refreshToken.setOnPreferenceClickListener {
            toastInfo("Refreshing your token...")
            authViewModel.getRefreshToken().observe(viewLifecycleOwner) { success ->
                if(success){
                    toastSuccess("Token refresh success!")
                    tokenValidity.summary = DateTimeUtil.convertEpochToHumanTime(accManager.tokenExpiry)
                } else {
                    toastError("There was an error refreshing your token")
                }
            }
            true
        }

        val certValue = findPreference<Preference>("cert_value") as Preference
        certValue.setOnPreferenceChangeListener{ _, newValue ->
            try {
                Base64.decode(newValue.toString(), Base64.DEFAULT).toString(Charsets.UTF_8)
            }catch (exception: IllegalArgumentException){
                AlertDialog.Builder(requireContext())
                        .setTitle("Error parsing Certificate pin value")
                        .setMessage("Your certificate pin is not a valid base64 value. The app will continue" +
                                " to work but you should note that certificate pinning is now useless.")
                        .setPositiveButton("OK") { _, _ -> }
                        .show()
            }
            FireflyClient.destroyInstance()
            true
        }
        val autoRefreshToken = findPreference<SwitchPreference>("auto_refresh_token") as SwitchPreference
        val refreshTokenInterval = findPreference<ListPreference>("refresh_token_interval") as ListPreference
        autoRefreshToken.setOnPreferenceChangeListener { _, newValue ->
            if(newValue == false){
                WorkManager.getInstance(requireContext()).cancelAllWorkByTag("refresh_worker")
            } else {
                AlertDialog.Builder(requireContext())
                        .setTitle(R.string.warning)
                        .setMessage("This feature may not work as expected on certain devices." +
                                "Currently, it ensures that your device is charging, connected to a " +
                                "network and the battery is not low before running.")
                        .setPositiveButton("OK") { _, _ -> }
                        .create()
                        .show()
            }
            true
        }
        refreshTokenInterval.setOnPreferenceChangeListener { _, newValue ->
            val workBuilder = PeriodicWorkRequest
                    .Builder(RefreshTokenWorker::class.java, newValue.toString().toLong(), TimeUnit.HOURS)
                    .addTag("refresh_worker")
                    .setConstraints(Constraints.Builder()
                            .setRequiresCharging(true)
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .setRequiresBatteryNotLow(true)
                            .build())
                    .build()
            WorkManager.getInstance(requireContext()).enqueue(workBuilder)
            true
        }
        val logout = findPreference<Preference>("logout") as Preference
        logout.setOnPreferenceClickListener {
            GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
                AppDatabase.clearDb(requireContext())
                AppPref(sharedPref).clearPref()
                accManager.destroyAccount()
            }
            val loginActivity = Intent(requireActivity(), AuthActivity::class.java)
            startActivity(loginActivity)
            FireflyClient.destroyInstance()
            requireActivity().finish()
            true
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().activity_toolbar.title = "Account Settings"
    }
}