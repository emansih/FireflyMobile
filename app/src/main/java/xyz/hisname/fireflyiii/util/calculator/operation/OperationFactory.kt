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

