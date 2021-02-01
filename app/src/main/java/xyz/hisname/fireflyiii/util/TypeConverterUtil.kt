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

package xyz.hisname.fireflyiii.util

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter
import com.squareup.moshi.*
import java.math.BigDecimal
import java.time.*

object TypeConverterUtil{

    @TypeConverter
    @JvmStatic
    fun toBoolean(value: Boolean): String{
        return value.toString()
    }

    @TypeConverter
    @JvmStatic
    fun fromBoolean(value: String): Boolean{
        return value.toBoolean()
    }

    @TypeConverter
    @JvmStatic
    fun fromBigDecimal(value: BigDecimal): String{
        return value.toPlainString()
    }

    @TypeConverter
    @JvmStatic
    fun toBigDecimal(value: String?): BigDecimal{
        return value?.toBigDecimal() ?: 0.toBigDecimal()
    }

    @TypeConverter
    @JvmStatic
    fun fromString(value: List<String>?): String{
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter: JsonAdapter<List<String>> = Moshi.Builder().build().adapter(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toList(value: String): List<String>{
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter: JsonAdapter<List<String>> = Moshi.Builder().build().adapter(type)
        return adapter.fromJson(value) ?: arrayListOf()
    }

    @TypeConverter
    @JvmStatic
    fun fromTimestamp(value: Long?): OffsetDateTime? {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(value ?: 0), ZoneId.systemDefault())
    }

    @TypeConverter
    @JvmStatic
    fun dateToTimestamp(date: OffsetDateTime?): Long? {
        return date?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    @JvmStatic
    fun fromLocalDate(value: String?): LocalDate? {
        return LocalDate.parse(value)
    }

    @TypeConverter
    @JvmStatic
    fun toLocalDate(value: LocalDate?): String {
        return value.toString()
    }

    @TypeConverter
    @JvmStatic
    fun fromUri(value: String?): Uri? {
        return value?.toUri()
    }

    @TypeConverter
    @JvmStatic
    fun toUri(value: Uri?): String? {
        return value?.toString()
    }
}
