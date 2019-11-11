package xyz.hisname.fireflyiii.util.calculator.operation

import xyz.hisname.fireflyiii.util.calculator.*
import xyz.hisname.fireflyiii.util.calculator.operation.base.Operation

object OperationFactory {

    fun forId(id: String, baseValue: Double, secondValue: Double): Operation? {
        return when (id) {
            PLUS -> PlusOperation(baseValue, secondValue)
            MINUS -> MinusOperation(baseValue, secondValue)
            DIVIDE -> DivideOperation(baseValue, secondValue)
            MULTIPLY -> MultiplyOperation(baseValue, secondValue)
            PERCENT -> PercentOperation(baseValue, secondValue)
            POWER -> PowerOperation(baseValue, secondValue)
            ROOT -> RootOperation(baseValue)
            FACTORIAL -> FactorialOperation(baseValue)
            else -> null
        }
    }
}

