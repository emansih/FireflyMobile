package xyz.hisname.fireflyiii.ui.settings

import android.content.Context
import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref

class DeveloperSettings: BaseSettings() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.developer_settings)
        setDelay()
    }

    private fun setDelay(){
        val workManagerDelay = findPreference<EditTextPreference>("workManagerDelayPref") as EditTextPreference
        workManagerDelay.summary = AppPref(sharedPref).workManagerDelay.toString() + "mins"
        workManagerDelay.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }
        workManagerDelay.setOnPreferenceChangeListener { _, newValue ->
            AppPref(sharedPref).workManagerDelay = newValue.toString().toLong()
            workManagerDelay.summary = AppPref(sharedPref).workManagerDelay.toString() + "mins"
            true
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.activity_toolbar?.title = "Let There Be Dragons"
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = "Let There Be Dragons"
    }
}