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

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import xyz.hisname.fireflyiii.R
import androidx.fragment.app.commit
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.*
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.util.biometric.KeyguardUtil
import xyz.hisname.languagepack.LanguageChanger
import java.io.File


class SettingsFragment: BaseSettings() {

    private lateinit var chooseFolder: ActivityResultLauncher<Uri>
    private lateinit var userDownloadDirectoryPref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_settings)
        setAccountSection()
        setTransactionSection()
        setLanguagePref()
        setNightModeSection()
        setPinCode()
        setThumbnail()
        setTutorial()
        setDeveloperOption()
        deleteItems()
        userDefinedDirectory()
    }

    private fun setLanguagePref(){
        val languagePref = findPreference<ListPreference>("language_pref") as ListPreference
        languagePref.value = AppPref(sharedPref).languagePref
        languagePref.setOnPreferenceChangeListener { _, newValue ->
            AppPref(sharedPref).languagePref = newValue.toString()
            LanguageChanger.init(requireContext(), AppPref(sharedPref).languagePref)
            val coordinatorLayout = requireActivity().findViewById<CoordinatorLayout>(R.id.coordinatorlayout)
            Snackbar.make(coordinatorLayout, "Restart to apply changes", Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok){
                        ActivityCompat.recreate(requireActivity())
                    }
                    .show()
            true
        }
        languagePref.icon = IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_language
            sizeDp = 24
            colorRes = setIconColor()
        }
    }

    private fun setAccountSection(){
        val accountOptions = findPreference<Preference>("account_options") as Preference
        accountOptions.setOnPreferenceClickListener {
            parentFragmentManager.commit {
                addToBackStack(null)
                setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                replace(R.id.fragment_container, SettingsAccountFragment())
            }
            true
        }
        accountOptions.icon = IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_account_circle
            sizeDp = 24
            colorRes = setIconColor()
        }
    }

    private fun setTransactionSection(){
        val transactionSettings = findPreference<Preference>("transaction_settings") as Preference
        transactionSettings.setOnPreferenceClickListener {
            parentFragmentManager.commit {
                addToBackStack(null)
                setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                replace(R.id.fragment_container, TransactionSettings())
            }
            true
        }

        transactionSettings.icon = IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_notifications
            sizeDp = 24
            colorRes = setIconColor()
        }
    }

    private fun setNightModeSection(){
        val nightModePref = findPreference<CheckBoxPreference>("night_mode") as CheckBoxPreference
        nightModePref.setOnPreferenceChangeListener { preference, newValue ->
            val nightMode = newValue as Boolean
            AppPref(sharedPref).nightModeEnabled = nightMode
            val coordinatorLayout = requireActivity().findViewById<CoordinatorLayout>(R.id.coordinatorlayout)
            Snackbar.make(coordinatorLayout, "Restart to apply changes", Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok){
                        ActivityCompat.recreate(requireActivity())
                    }
                    .show()
            true
        }
        nightModePref.icon = IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_moon
            sizeDp = 24
            colorRes = setIconColor()
        }
    }

    private fun setThumbnail(){
        val thumbnailPref = findPreference<CheckBoxPreference>("currencyThumbnail") as CheckBoxPreference
        thumbnailPref.icon = IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_attach_money
            sizeDp = 24
            colorRes = setIconColor()
        }
        thumbnailPref.setOnPreferenceChangeListener { preference, newValue ->
            val thumbNail = newValue as Boolean
            AppPref(sharedPref).isCurrencyThumbnailEnabled = thumbNail
            true
        }
    }

    private fun setTutorial(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val tutorialSetting = findPreference<Preference>("tutorial_setting") as Preference
            tutorialSetting.isVisible = true
            tutorialSetting.icon = IconicsDrawable(requireContext()).apply {
                icon = FontAwesome.Icon.faw_university
                sizeDp = 24
                colorRes = setIconColor()
            }
            tutorialSetting.setOnPreferenceClickListener {
                requireContext().deleteSharedPreferences("PrefShowCaseView")
                true
            }
        }
    }

    private fun setDeveloperOption(){
        val developerSettings = findPreference<Preference>("developer_options") as Preference
        developerSettings.setOnPreferenceClickListener {
            parentFragmentManager.commit {
                addToBackStack(null)
                setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                replace(R.id.fragment_container, DeveloperSettings())
            }
            true
        }
        developerSettings.icon = IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_developer_mode
            sizeDp = 24
            colorRes = setIconColor()
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = resources.getString(R.string.settings)
    }

    private fun setIconColor(): Int{
        return if(globalViewModel.isDark){
            R.color.md_white_1000
        } else {
            R.color.md_black_1000
        }
    }

    private fun setPinCode(){
        val keyguardPref = findPreference<Preference>("keyguard") as Preference
        if(!KeyguardUtil(requireActivity()).isDeviceKeyguardEnabled() || BiometricManager.from(requireContext()).canAuthenticate(
                BiometricManager.Authenticators.DEVICE_CREDENTIAL) != BIOMETRIC_SUCCESS){
            keyguardPref.isSelectable = false
            keyguardPref.summary = "Please enable pin / password / biometrics in your device settings"
        }
        keyguardPref.icon = IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_lock
            sizeDp = 24
            colorRes = setIconColor()
        }
        keyguardPref.setOnPreferenceChangeListener { preference, newValue ->
            val keyGuard = newValue as Boolean
            AppPref(sharedPref).isKeyguardEnabled = keyGuard
            true
        }
    }

    private fun deleteItems(){
        val deleteData = findPreference<Preference>("delete_data") as Preference
        deleteData.icon = IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_delete_forever
            sizeDp = 24
            colorRes = setIconColor()
        }
        deleteData.setOnPreferenceClickListener {
            parentFragmentManager.commit {
                addToBackStack(null)
                setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                replace(R.id.fragment_container, DeleteItemsFragment())
            }
            true
        }
    }

    private fun userDefinedDirectory(){
        userDownloadDirectoryPref = findPreference<Preference>("userDefinedDownloadDirectory") as Preference
        val userPref = AppPref(sharedPref).userDefinedDownloadDirectory
        userDownloadDirectoryPref.icon = IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_file_download
            sizeDp = 24
            colorRes = setIconColor()
        }
        val userDirectory = if(userPref.isEmpty()){
            File(requireContext().getExternalFilesDir(null).toString()).toString()
        } else {
            userPref
        }
        userDownloadDirectoryPref.summary = userDirectory
        userDownloadDirectoryPref.setOnPreferenceClickListener {
            chooseFolder.launch(Uri.EMPTY)
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chooseFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { folderChoosen ->
            if(folderChoosen != null){
                AppPref(sharedPref).userDefinedDownloadDirectory = folderChoosen.toString()
                userDownloadDirectoryPref.summary = folderChoosen.toString()
            }
        }
    }
}