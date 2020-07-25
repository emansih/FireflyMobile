package xyz.hisname.fireflyiii.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.Spent
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import java.math.BigDecimal
import java.time.*

object GsonConverterUtil{

    @TypeConverter
    @JvmStatic
    fun toBoolean(value: Boolean): String{
        return Gson().toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun fromBoolean(value: String): Boolean{
        return Gson().fromJson(value, Boolean::class.java)

    }

    @TypeConverter
    @JvmStatic
    fun toBigDecimal(value: BigDecimal): String{
        return Gson().toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun fromBigDecimal(value: String?): BigDecimal?{
        return Gson().fromJson(value, BigDecimal::class.java)
    }

    @TypeConverter
    @JvmStatic
    fun fromString(value: List<String>): String{
        val type = object : TypeToken<List<String>>(){}.type
        return Gson().toJson(value,type)
    }

    @TypeConverter
    @JvmStatic
    fun toList(value: String): List<String>{
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value,type)
    }

    @TypeConverter
    @JvmStatic
    fun fromBudgetSpent(spent: List<Spent>): String{
        val type = object : TypeToken<List<Spent>>(){}.type
        return Gson().toJson(spent,type)
    }

    @TypeConverter
    @JvmStatic
    fun toBudgetSpent(spent: String): List<Spent>{
        val type = object : TypeToken<List<Spent>>() {}.type
        return Gson().fromJson(spent,type)
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
    fun toDouble(value: Double): String{
        return Gson().toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun fromDouble(value: String): Double{
        return Gson().fromJson(value, Double::class.java)
    }

    @TypeConverter
    @JvmStatic
    fun toTransactionList(value: List<Transactions>): String{
        val type = object : TypeToken<List<Transactions>>() {}.type
        return Gson().toJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromTransactionList(value: String): List<Transactions>{
        val type = object : TypeToken<List<Transactions>>() {}.type
        return Gson().fromJson(value,type)
    }
}
