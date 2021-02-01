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

package xyz.hisname.fireflyiii.util.extension

import android.widget.EditText
import androidx.core.text.isDigitsOnly
import com.google.android.material.textfield.TextInputLayout
import xyz.hisname.fireflyiii.R

fun EditText.getString(): String {
    return text.toString()
}

fun EditText.isBlank(): Boolean {
    return getString().isBlank()
}

fun EditText.getDigits(): Int {
    return getString().toInt()
}

fun EditText.isDigitsOnly(): Boolean {
    return getString().isDigitsOnly()
}

fun EditText.showRequiredError(){
    this.error = resources.getText(R.string.required_field)
}

fun TextInputLayout.showRequiredError(){
    this.error = resources.getText(R.string.required_field)
}
