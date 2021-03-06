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
