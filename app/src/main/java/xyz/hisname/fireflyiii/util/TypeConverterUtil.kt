package xyz.hisname.fireflyiii.util

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
    fun toBigDecimal(value: BigDecimal): String{
        return value.toString()
    }

    @TypeConverter
    @JvmStatic
    fun fromBigDecimal(value: String?): BigDecimal?{
        return BigDecimal(value)
    }

    @TypeConverter
    @JvmStatic
    fun fromString(value: List<String>): String{
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
}
