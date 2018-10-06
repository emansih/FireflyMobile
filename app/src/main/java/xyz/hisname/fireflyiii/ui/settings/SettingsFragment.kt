package xyz.hisname.fireflyiii.ui.settings

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.preference.PreferenceFragmentCompat
import xyz.hisname.fireflyiii.R
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.preference.EditTextPreference
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.util.extension.toastInfo


class SettingsFragment: PreferenceFragmentCompat() {

    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private val fireflyUrl by lazy { sharedPref.getString("fireflyUrl","") ?: "" }
    private val fireflySecretKey by lazy { sharedPref.getString("fireflySecretKey","") ?: "" }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_settings)
        setAccountSection()
    }

    private fun setAccountSection(){
        val fireflyUrlPref = findPreference("fireflyUrl") as EditTextPreference
        fireflyUrlPref.apply {
            title = "Firefly URL"
            summary = fireflyUrl
        }

        val accessTokenPref = findPreference("access_token") as EditTextPreference
        accessTokenPref.apply {
            title = "Access Token"
            summary = fireflySecretKey
        }

        fireflyUrlPref.setOnPreferenceChangeListener { preference, newValue  ->
            preference.summary = newValue.toString()
            toastInfo("You should also change your access token", Toast.LENGTH_LONG)
            RetrofitBuilder.destroyInstance()
             true
        }
        accessTokenPref.setOnPreferenceChangeListener { preference, newValue  ->
            preference.summary = newValue.toString()
            RetrofitBuilder.destroyInstance()
            true
        }
    }

    override fun setDivider(divider: Drawable) {
        super.setDivider(ColorDrawable(Color.GRAY))
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        requireActivity().activity_toolbar.title = "Settings"
    }

    override fun onResume() {
        super.onResume()
        requireActivity().activity_toolbar.title = "Settings"
    }
}