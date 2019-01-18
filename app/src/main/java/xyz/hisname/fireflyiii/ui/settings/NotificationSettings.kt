package xyz.hisname.fireflyiii.ui.settings

import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import androidx.preference.CheckBoxPreference
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils

class NotificationSettings: BaseSettings() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_notification_settings)
        val transactionPref = findPreference("persistent_notification") as CheckBoxPreference
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
        activity?.activity_toolbar?.title = "Notifications"
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = "Notifications"
    }
}