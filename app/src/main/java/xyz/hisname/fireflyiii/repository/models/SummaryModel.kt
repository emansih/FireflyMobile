/*
 * Copyright (c)  2018 - 2021 Daniel Quah
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

package xyz.hisname.fireflyiii.repository.models

import com.squareup.moshi.JsonClass
import org.json.JSONObject

// https://stackoverflow.com/questions/59066476/how-do-i-use-moshi-to-serialize-a-json-string-into-org-json-jsonobject
@JsonClass(generateAdapter = true)
data class SummaryModel(
        val someProperty: String,
        val someDynamicProperty: JSONObject?
)
