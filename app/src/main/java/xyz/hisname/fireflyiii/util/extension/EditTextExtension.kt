package xyz.hisname.fireflyiii.util.extension

import android.widget.EditText
import androidx.core.text.isDigitsOnly

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