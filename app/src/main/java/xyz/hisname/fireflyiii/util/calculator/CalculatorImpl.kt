/*
 * Copyright (c)  2018 - 2021 Daniel Quah and SimpleMobileTools
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

package xyz.hisname.fireflyiii.util.calculator

import android.content.Context
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.calculator.operation.OperationFactory

class CalculatorImpl(val calculator: Calculator, val context: Context,
                     var displayedNumber: String) {


    private var lastKey: String? = null
    private var lastOperation: String = ""

    private var isFirstOperation = false
    private var resetValue = false
    private var wasPercentLast = false
    private var baseValue = 0.0
    private var secondValue = 0.0

    init {
        baseValue = displayedNumber.toDouble()
    }

    fun handleOperation(operation: String) {
        wasPercentLast = operation == PERCENT
        if (lastKey == DIGIT && operation != ROOT && operation != FACTORIAL) {
            handleResult()
        }

        resetValue = true
        lastKey = operation
        lastOperation = operation

        if (operation == ROOT) {
            handleRoot()
            resetValue = false
        }
        if (operation == FACTORIAL) {
            handleFactorial()
            resetValue = false
        }
    }

    fun handleClear() {
        if (displayedNumber == NAN) {
            handleReset()
        } else {
            val oldValue = displayedNumber
            var newValue = "0"
            val len = oldValue.length
            var minLen = 1
            if (oldValue.contains("-"))
                minLen++

            if (len > minLen) {
                newValue = oldValue.substring(0, len - 1)
            }

            newValue = newValue.replace("\\.$".toRegex(), "")
            newValue = formatString(newValue)
            setValue(newValue)
            baseValue = Formatter.stringToDouble(newValue)
        }
    }

    fun handleReset() {
        resetValues()
        setValue("0")
        setFormula("")
    }

    fun handleEquals() {
        if (lastKey == EQUALS)
            calculateResult()

        if (lastKey != DIGIT)
            return

        secondValue = getDisplayedNumberAsDouble()
        calculateResult()
        lastKey = EQUALS
    }



    private fun setValue(value: String) {
        calculator.setValue(value, context)
        displayedNumber = value
    }

    private fun resetValueIfNeeded() {
        if (resetValue)
            displayedNumber = "0"

        resetValue = false
    }

    private fun resetValues() {
        resetValue = false
        lastOperation = ""
        displayedNumber = ""
        isFirstOperation = true
        lastKey = ""
    }

    private fun setFormula(value: String) {
        calculator.setFormula(value, context)
    }

    private fun updateFormula() {
        val first = Formatter.doubleToString(baseValue)
        val second = Formatter.doubleToString(secondValue)
        val sign = getSign(lastOperation)

        when {
            sign == "√" -> setFormula(sign + first)
            sign == "!" -> setFormula(first + sign)
            sign.isNotEmpty() -> {
                var formula = first + sign + second
                if (wasPercentLast) {
                    formula += "%"
                }
                setFormula(formula)
            }
        }
    }

    private fun addDigit(number: Int) = setValue(formatString(displayedNumber + number))


    private fun formatString(str: String): String {
        // if the number contains a decimal, do not try removing the leading zero anymore, nor add group separator
        // it would prevent writing values like 1.02
        if (str.contains(".")) {
            return str
        }

        val doubleValue = Formatter.stringToDouble(str)
        return Formatter.doubleToString(doubleValue)
    }

    private fun updateResult(value: Double) {
        setValue(Formatter.doubleToString(value))
        baseValue = value
    }

    private fun getDisplayedNumberAsDouble() = Formatter.stringToDouble(displayedNumber)

    private fun handleResult() {
        secondValue = getDisplayedNumberAsDouble()
        calculateResult()
        baseValue = getDisplayedNumberAsDouble()
    }

    private fun handleRoot() {
        baseValue = getDisplayedNumberAsDouble()
        calculateResult()
    }

    private fun handleFactorial() {
        baseValue = getDisplayedNumberAsDouble()
        calculateResult()
    }

    private fun calculateResult() {
        updateFormula()
        if (wasPercentLast) {
            secondValue *= baseValue / 100
        }

        val operation = OperationFactory.forId(lastOperation, baseValue, secondValue)
        if (operation != null) {
            updateResult(operation.getResult())
        }

        isFirstOperation = false
    }

    private fun decimalClicked() {
        var value = displayedNumber
        if (!value.contains(".")) {
            value += "."
        }
        setValue(value)
    }

    private fun zeroClicked() {
        val value = displayedNumber
        if (value != "0") {
            addDigit(0)
        }
    }

    private fun getSign(lastOperation: String?) = when (lastOperation) {
        PLUS -> "+"
        MINUS -> "-"
        MULTIPLY -> "*"
        DIVIDE -> "/"
        PERCENT -> "%"
        POWER -> "^"
        ROOT -> "√"
        FACTORIAL -> "!"
        else -> ""
    }

    fun numpadClicked(id: Int) {
        if (lastKey == EQUALS) {
            lastOperation = EQUALS
        }

        lastKey = DIGIT
        resetValueIfNeeded()

        if(displayedNumber == "0.0"){
            displayedNumber = ""
        }

        when (id) {
            R.id.btn_decimal -> decimalClicked()
            R.id.btn_0 -> zeroClicked()
            R.id.btn_1 -> addDigit(1)
            R.id.btn_2 -> addDigit(2)
            R.id.btn_3 -> addDigit(3)
            R.id.btn_4 -> addDigit(4)
            R.id.btn_5 -> addDigit(5)
            R.id.btn_6 -> addDigit(6)
            R.id.btn_7 -> addDigit(7)
            R.id.btn_8 -> addDigit(8)
            R.id.btn_9 -> addDigit(9)
        }
    }
}