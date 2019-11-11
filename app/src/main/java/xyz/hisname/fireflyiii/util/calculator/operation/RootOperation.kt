package xyz.hisname.fireflyiii.util.calculator.operation

import xyz.hisname.fireflyiii.util.calculator.operation.base.Operation
import xyz.hisname.fireflyiii.util.calculator.operation.base.UnaryOperation
import kotlin.math.sqrt

class RootOperation(value: Double) : UnaryOperation(value), Operation {
    override fun getResult() = sqrt(value)
}
