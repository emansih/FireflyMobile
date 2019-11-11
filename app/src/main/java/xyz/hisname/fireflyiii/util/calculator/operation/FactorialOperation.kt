package xyz.hisname.fireflyiii.util.calculator.operation

import xyz.hisname.fireflyiii.util.calculator.operation.base.Operation
import xyz.hisname.fireflyiii.util.calculator.operation.base.UnaryOperation

class FactorialOperation(value: Double) : UnaryOperation(value), Operation {

    override fun getResult(): Double{
        var result = 1.0
        if (value == 0.0 || value == 1.0){
            return result
        }else{
            val base = value.toInt()
            for(i in 1..base){
                result *= i
            }
        }
        return result
    }
}
