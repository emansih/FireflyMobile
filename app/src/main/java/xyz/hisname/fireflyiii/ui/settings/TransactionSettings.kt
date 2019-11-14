package xyz.hisname.fireflyiii.ui.settings

import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import androidx.preference.CheckBoxPreference
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils

class TransactionSettings: BaseSettings() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_transaction_settings)
        addPermNotif()
    }

    private fun addPermNotif(){
        val transactionPref = findPreference<CheckBoxPreference>("persistent_notification") as CheckBoxPreference
        val notification = NotificationUtils(requireContext())
        transactionPref.setOnPreferenceChangeListener { _, newValue ->
            if(newValue == true){
                notification.showTransactionPersistentNotification()
            } else {
                NotificationManagerCompat.from(requireContext()).cancel("transaction_notif",12345)
            }
            true
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.activity_toolbar?.title = "Transaction Settings"
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = "Transaction Settings"
    }

    override fun handleBack() {
        requireParentFragment().parentFragmentManager.popBackStack()
    }
}