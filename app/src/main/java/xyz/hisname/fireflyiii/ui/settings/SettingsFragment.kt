package xyz.hisname.fireflyiii.ui.settings

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import xyz.hisname.fireflyiii.R
import androidx.fragment.app.commit
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.languagepack.LanguageChanger


class SettingsFragment: BaseSettings() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_settings)
        setAccountSection()
        setTransactionSection()
        setLanguagePref()
        setNightModeSection()
    }

    private fun setLanguagePref(){
        val languagePref = findPreference("language_pref") as ListPreference
        languagePref.value = AppPref(sharedPref).languagePref
        languagePref.setOnPreferenceChangeListener { _, newValue ->
            AppPref(sharedPref).languagePref = newValue.toString()
            LanguageChanger.init(requireContext(), AppPref(sharedPref).languagePref)
            requireActivity().recreate()
            true
        }
        languagePref.icon =  IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_language).sizeDp(24)
    }

    private fun setAccountSection(){
        val accountOptions = findPreference("account_options") as Preference
        accountOptions.setOnPreferenceClickListener {
            requireFragmentManager().commit {
                addToBackStack(null)
                setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                replace(R.id.fragment_container, SettingsAccountFragment())
            }
            true
        }
        accountOptions.icon = IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_account_circle).sizeDp(24)
    }

    private fun setTransactionSection(){
        val notificationPref = findPreference("notification_settings") as Preference
        notificationPref.setOnPreferenceClickListener {
            requireFragmentManager().commit {
                addToBackStack(null)
                setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                replace(R.id.fragment_container, NotificationSettings())
            }
            true
        }
        notificationPref.icon = IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_notifications).sizeDp(24)
    }

    private fun setNightModeSection(){
        val nightModePref = findPreference("night_mode") as CheckBoxPreference
        nightModePref.setOnPreferenceChangeListener { preference, newValue ->
            val nightMode = newValue as Boolean
            AppPref(sharedPref).nightModeEnabled = nightMode
            globalViewModel.handleNightMode(nightMode)
            true
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.settings)
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = resources.getString(R.string.settings)
    }

    override fun handleBack() {
        requireFragmentManager().popBackStack()
    }
}