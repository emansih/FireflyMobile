package xyz.hisname.fireflyiii.util.calculator.operation

import xyz.hisname.fireflyiii.util.calculator.operation.base.BinaryOperation
import xyz.hisname.fireflyiii.util.calculator.operation.base.Operation

class MinusOperation(baseValue: Double, secondValue: Double) : BinaryOperation(baseValue, secondValue), Operation {
    override fun getResult() = baseValue - secondValue
}
