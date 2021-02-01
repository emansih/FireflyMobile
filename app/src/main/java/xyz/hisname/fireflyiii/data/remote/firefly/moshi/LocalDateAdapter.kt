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

package xyz.hisname.fireflyiii.data.remote.firefly.moshi

import com.squareup.moshi.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateAdapter: JsonAdapter<LocalDate>(){
    override fun toJson(writer: JsonWriter, value: LocalDate?) {
        value?.let { writer.value(it.format(formatter)) }
    }

    override fun fromJson(reader: JsonReader): LocalDate? {
        return if (reader.peek() != JsonReader.Token.NULL) {
            fromNonNullString(reader.nextString())
        } else {
            reader.nextNull<Any>()
            null
        }
    }

    private val formatter = DateTimeFormatter.ISO_DATE
    private fun fromNonNullString(nextString: String): LocalDate = LocalDate.parse(nextString, formatter)

}