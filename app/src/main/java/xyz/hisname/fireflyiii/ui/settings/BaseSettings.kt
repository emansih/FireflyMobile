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

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceFragmentCompat
import xyz.hisname.fireflyiii.repository.GlobalViewModel
import xyz.hisname.fireflyiii.util.extension.getViewModel

abstract class BaseSettings: PreferenceFragmentCompat() {

    protected val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    protected val globalViewModel by lazy { getViewModel(GlobalViewModel::class.java) }

    override fun setDivider(divider: Drawable) {
        super.setDivider(ColorDrawable(Color.GRAY))
    }
}