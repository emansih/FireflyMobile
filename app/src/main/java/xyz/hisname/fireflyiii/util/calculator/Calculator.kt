package xyz.hisname.fireflyiii.util.calculator

import android.content.Context

interface Calculator {
    fun setValue(value: String, context: Context)

    fun setFormula(value: String, context: Context)
}
