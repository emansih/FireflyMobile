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

import android.content.Context
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationManagerCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import xyz.hisname.fireflyiii.util.extension.toastInfo

class TransactionSettings: BaseSettings() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_transaction_settings)
        addPermNotif()
        setDateTimeFormat()
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


    private fun setDateTimeFormat(){
        val dateTimeFormat = findPreference<ListPreference>("dateTimeFormat") as ListPreference
        dateTimeFormat.setOnPreferenceChangeListener { preference, newValue ->
            AppPref(sharedPref).dateTimeFormat = newValue.toString().toInt()
            true
        }
        if(AppPref(sharedPref).userDefinedDateTimeFormat.isNotEmpty()){
            dateTimeFormat.isEnabled = false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = "Transaction Settings"
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = "Transaction Settings"
    }
}