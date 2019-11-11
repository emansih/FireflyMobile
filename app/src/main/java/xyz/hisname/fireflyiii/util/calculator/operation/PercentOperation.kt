package xyz.hisname.fireflyiii.util.calculator.operation

import xyz.hisname.fireflyiii.util.calculator.operation.base.BinaryOperation
import xyz.hisname.fireflyiii.util.calculator.operation.base.Operation

class PercentOperation(baseValue: Double, secondValue: Double) : BinaryOperation(baseValue, secondValue), Operation {

    override fun getResult(): Double {
        var result = 0.0
        if (secondValue != 0.0) {
            result = baseValue / 100 * secondValue
        }
        return result
    }
}
