package xyz.hisname.fireflyiii.util.calculator.operation

import xyz.hisname.fireflyiii.util.calculator.operation.base.BinaryOperation
import xyz.hisname.fireflyiii.util.calculator.operation.base.Operation
import kotlin.math.pow

class PowerOperation(baseValue: Double, secondValue: Double) : BinaryOperation(baseValue, secondValue), Operation {

    override fun getResult(): Double {
        var result = baseValue.pow(secondValue)
        if (result.isInfinite() || result.isNaN())
            result = 0.0
        return result
    }
}
