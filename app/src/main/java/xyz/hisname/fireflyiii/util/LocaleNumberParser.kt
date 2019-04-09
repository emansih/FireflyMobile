package xyz.hisname.fireflyiii.util

import android.content.Context
import xyz.hisname.languagepack.LanguageChanger
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class LocaleNumberParser {

    companion object {
        fun parseDecimal(input: Double, context: Context): Double{
            val formatter = DecimalFormat("#0.00")
            val nf = NumberFormat.getInstance(LanguageChanger.getLocale(context))
            return nf.parse(formatter.format(input)).toDouble()
        }
    }

}