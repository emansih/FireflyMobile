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
import android.text.InputType
import androidx.appcompat.widget.Toolbar
import androidx.preference.EditTextPreference
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
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = "Let There Be Dragons"
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = "Let There Be Dragons"
    }
}