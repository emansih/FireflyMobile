package xyz.hisname.fireflyiii.util.extension

import android.widget.EditText
import androidx.core.text.isDigitsOnly
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
