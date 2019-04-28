package xyz.hisname.fireflyiii.ui.settings

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat
import xyz.hisname.fireflyiii.R
import androidx.fragment.app.commit
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.util.KeyguardUtil
import xyz.hisname.languagepack.LanguageChanger


class SettingsFragment: BaseSettings() {

    private val nightMode by lazy {  AppPref(PreferenceManager.getDefaultSharedPreferences(requireContext())).nightModeEnabled }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_settings)
        setAccountSection()
        setTransactionSection()
        setLanguagePref()
        setNightModeSection()
        setPinCode()
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
        languagePref.icon = IconicsDrawable(requireContext())
                .icon(GoogleMaterial.Icon.gmd_language)
                .sizeDp(24).setIconColor()
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
        accountOptions.icon = IconicsDrawable(requireContext())
                .icon(GoogleMaterial.Icon.gmd_account_circle)
                .sizeDp(24).setIconColor()
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

        notificationPref.icon = IconicsDrawable(requireContext())
                .icon(GoogleMaterial.Icon.gmd_notifications)
                .sizeDp(24).setIconColor()
    }

    private fun setNightModeSection(){
        val nightModePref = findPreference("night_mode") as CheckBoxPreference
        nightModePref.setOnPreferenceChangeListener { preference, newValue ->
            val nightMode = newValue as Boolean
            AppPref(sharedPref).nightModeEnabled = nightMode
            true
        }
        nightModePref.icon = IconicsDrawable(requireContext())
                .icon(GoogleMaterial.Icon.gmd_invert_colors)
                .sizeDp(24).setIconColor()
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

    private fun IconicsDrawable.setIconColor(): Drawable{
        return if(nightMode){
            this.color(ContextCompat.getColor(requireContext(), R.color.md_white_1000))
        } else {
            this.color(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
        }
    }

    private fun setPinCode(){
        val keyguardPref = findPreference("keyguard") as Preference
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ){
            keyguardPref.isVisible = false
        }
        if(!KeyguardUtil(requireActivity()).isDeviceKeyguardEnabled()){
            keyguardPref.isSelectable = false
            keyguardPref.summary = "Please enable pin / password / biometrics in your device settings"
        }
    }
}