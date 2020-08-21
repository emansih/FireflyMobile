package xyz.hisname.fireflyiii.ui.settings

import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.R

class DeveloperSettings: BaseSettings() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.developer_settings)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.activity_toolbar?.title = "Let There Be Dragons"
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = "Let There Be Dragons"
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}