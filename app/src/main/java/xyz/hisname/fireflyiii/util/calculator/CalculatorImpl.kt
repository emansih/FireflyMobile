package xyz.hisname.fireflyiii.util.calculator

import android.content.Context
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.calculator.operation.OperationFactory

class CalculatorImpl(val calculator: Calculator, val context: Context,
                     var displayedNumber: String) {


    private var lastKey: String? = null
    private var mLastOperation: String = ""

    private var mIsFirstOperation = false
    private var mResetValue = false
    private var mWasPercentLast = false
    private var mBaseValue = 0.0
    private var mSecondValue = 0.0

    init {
        mBaseValue = displayedNumber.toDouble()
    }

    fun handleOperation(operation: String) {
        mWasPercentLast = operation == PERCENT
        if (lastKey == DIGIT && operation != ROOT && operation != FACTORIAL) {
            handleResult()
        }

        mResetValue = true
        lastKey = operation
        mLastOperation = operation

        if (operation == ROOT) {
            handleRoot()
            mResetValue = false
        }
        if (operation == FACTORIAL) {
            handleFactorial()
            mResetValue = false
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
            mBaseValue = Formatter.stringToDouble(newValue)
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

        mSecondValue = getDisplayedNumberAsDouble()
        calculateResult()
        lastKey = EQUALS
    }



    private fun setValue(value: String) {
        calculator.setValue(value, context)
        displayedNumber = value
    }

    private fun resetValueIfNeeded() {
        if (mResetValue)
            displayedNumber = "0"

        mResetValue = false
    }

    private fun resetValues() {
        mResetValue = false
        mLastOperation = ""
        displayedNumber = ""
        mIsFirstOperation = true
        lastKey = ""
    }

    private fun setFormula(value: String) {
        calculator.setFormula(value, context)
    }

    private fun updateFormula() {
        val first = Formatter.doubleToString(mBaseValue)
        val second = Formatter.doubleToString(mSecondValue)
        val sign = getSign(mLastOperation)

        when {
            sign == "√" -> setFormula(sign + first)
            sign == "!" -> setFormula(first + sign)
            sign.isNotEmpty() -> {
                var formula = first + sign + second
                if (mWasPercentLast) {
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
        mBaseValue = value
    }

    private fun getDisplayedNumberAsDouble() = Formatter.stringToDouble(displayedNumber)

    private fun handleResult() {
        mSecondValue = getDisplayedNumberAsDouble()
        calculateResult()
        mBaseValue = getDisplayedNumberAsDouble()
    }

    private fun handleRoot() {
        mBaseValue = getDisplayedNumberAsDouble()
        calculateResult()
    }

    private fun handleFactorial() {
        mBaseValue = getDisplayedNumberAsDouble()
        calculateResult()
    }

    private fun calculateResult() {
        updateFormula()
        if (mWasPercentLast) {
            mSecondValue *= mBaseValue / 100
        }

        val operation = OperationFactory.forId(mLastOperation, mBaseValue, mSecondValue)
        if (operation != null) {
            updateResult(operation.getResult())
        }

        mIsFirstOperation = false
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
            mLastOperation = EQUALS
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