package xyz.hisname.fireflyiii.util

import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.TemporalAdjusters.*
import java.time.temporal.WeekFields
import java.lang.Long.parseLong
import java.time.*
import java.util.*

object DateTimeUtil {

    fun getCalToString(date: String): String{
        return Instant.ofEpochMilli(parseLong(date))
                .atOffset(OffsetDateTime.now().offset)
                .toLocalDate()
                .toString()
    }

    fun getStartOfDayInCalendarToEpoch(calendarDate: String): String{
        val startOfDay = LocalDate.parse(calendarDate).atStartOfDay(ZoneId.of(ZoneId.systemDefault().id)).toEpochSecond()
        val startOfDayMilli = startOfDay.times(1000)
        return startOfDayMilli.toString()
    }


     /*
     * 1 Day has 86400 seconds. (86400000 milliseconds)
     * So 1 second before midnight is considered a previous day
     * Start of day + 86400 - 1
     */
    fun getEndOfDayInCalendarToEpoch(calendarDate: String): String{
         val startOfDay = LocalDate.parse(calendarDate).atStartOfDay(ZoneId.of(ZoneId.systemDefault().id)).toEpochSecond()
         val endOfDayMilli = (startOfDay * 1000) + 86400000 - 1000
         return endOfDayMilli.toString()
    }


    /*
     * Takes in end date in yyyy-MM-dd format
     * Output difference in *DAYS*
     */
    fun getDaysDifference(date: String?): String {
        val todayDate = LocalDateTime.now().toLocalDate()
        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return Duration.between(todayDate.atStartOfDay(), localDate.atStartOfDay()).toDays().toString()
    }

    /*
    Accepts only yyyy-MM-dd parameter.
    Output day in short form
     */
    fun getDayOfWeek(date: String): String{
        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return localDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    // Returns end of month date in yyyy-MM-dd (2018-01-04)
    fun getEndOfMonth(): String{
        val localDateTime = LocalDate.now()
        val localDate = LocalDate.of(localDateTime.year, localDateTime.monthValue, localDateTime.dayOfMonth)
        return localDate.withDayOfMonth(localDate.lengthOfMonth()).toString()
    }

    fun getEndOfMonth(duration: Long): String{
        val localDateTime = LocalDate.now()
        val previousMonth = localDateTime.minusMonths(duration)
        val previousLocalDate =
                LocalDate.of(previousMonth.year, previousMonth.monthValue, previousMonth.dayOfMonth)
        return previousLocalDate.withDayOfMonth(previousMonth.lengthOfMonth()).toString()
    }

    fun getStartOfMonth(duration: Long): String {
        val localDateTime = LocalDate.now()
        val previousMonth = localDateTime.minusMonths(duration)
        val previousLocalDate =
                LocalDate.of(previousMonth.year, previousMonth.monthValue, previousMonth.dayOfMonth)
        return previousLocalDate.with(firstDayOfMonth()).toString()
    }

    fun getStartOfMonth(): String{
        val localDateTime = LocalDate.now()
        val localDate = LocalDate.of(localDateTime.year, localDateTime.monthValue, localDateTime.dayOfMonth)
        return localDate.with(firstDayOfMonth()).toString()
    }

    fun getStartOfYear(): String {
        val localDateTime = LocalDate.now()
        val localDate = LocalDate.of(localDateTime.year, localDateTime.monthValue, localDateTime.dayOfMonth)
        return localDate.with(firstDayOfYear()).toString()
    }

    fun getEndOfYear(): String {
        val localDateTime = LocalDate.now()
        val localDate = LocalDate.of(localDateTime.year, localDateTime.monthValue, localDateTime.dayOfMonth)
        return localDate.with(lastDayOfYear()).toString()
    }

    fun getStartOfWeek(): String {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(firstDayOfWeek)).toString()
    }

    fun getEndOfWeek(): String {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        val lastDayOfWeek = DayOfWeek.of(((firstDayOfWeek.value + 5) %
                DayOfWeek.values().size) + 1)
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(lastDayOfWeek)).toString()
    }

    fun getCurrentMonth(): String {
        val localDateTime = LocalDate.now()
        val localDate = LocalDate.of(localDateTime.year, localDateTime.monthValue, localDateTime.dayOfMonth)
        return localDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    fun getCurrentMonthShortName(): String{
        val localDateTime = LocalDate.now()
        val localDate = LocalDate.of(localDateTime.year, localDateTime.monthValue, localDateTime.dayOfMonth)
        return localDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    fun getPreviousMonth(duration: Long): String{
        val localDateTime = LocalDate.now()
        return localDateTime.minusMonths(duration).month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    fun getPreviousMonthShortName(duration: Long): String{
        val localDateTime = LocalDate.now()
        return localDateTime.minusMonths(duration).month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    fun getTodayDate(): String{
        return LocalDate.now().toString()
    }

    fun getWeeksBefore(date: String, weeks: Long): String {
        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return localDate.minusWeeks(weeks).toString()
    }

    fun getDaysBefore(date: String, days: Long): String{
        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return localDate.minusDays(days).toString()
    }

    // Outputs date in dd/MM
    fun getDayAndMonth(date: String): String{
        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return localDate.dayOfMonth.toString() + "/" + localDate.monthValue.toString()
    }

    // Outputs date in MM YY
    // Mar 2019
    fun getMonthAndYear(date: String): String{
        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return localDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).toString() +
                " " + localDate.year.toString()
    }

    // Outputs dd mm yyyy - dd mm yyyy
    // Example: 1 Apr 2019 -  30 Apr 2019
    fun getDurationText(): String{
        val localDateTime = LocalDate.now()
        val startOfMonth = LocalDate.of(localDateTime.year, localDateTime.monthValue,
                localDateTime.dayOfMonth).with(firstDayOfMonth())
        val endOfMonth = LocalDate.of(localDateTime.year, localDateTime.monthValue,
                localDateTime.dayOfMonth).with(lastDayOfMonth())
        val startDate = startOfMonth.dayOfMonth.toString() + " "  + startOfMonth.month.getDisplayName(TextStyle.SHORT,
                Locale.getDefault()) + " " + startOfMonth.year
        val endDate = endOfMonth.dayOfMonth.toString() + " "  + endOfMonth.month.getDisplayName(TextStyle.SHORT,
                Locale.getDefault()) + " " + endOfMonth.year
        return "$startDate - $endDate"
    }

    // Input LocalDateTime(yyyy-MM-dd T HH:mm)
    // Output String(yyyy-MM-dd @ HH:mm OR hh:mm)
    fun convertLocalDateTime(timeToParse: OffsetDateTime, shouldUse24HourFormat: Boolean): String {
        val timeFormat = if (!shouldUse24HourFormat){
            timeToParse.format(DateTimeFormatter.ofPattern("hh:mm a"))
        } else {
            timeToParse.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        return timeToParse.year.toString() + "-" + timeToParse.monthValue + "-" + timeToParse.dayOfMonth + " @ " +
                timeFormat
    }

    fun convertIso8601ToHumanDate(timeToParse: OffsetDateTime): String {
        val month = if ((timeToParse.monthValue + 1 ) < 10){
             "0"+ (timeToParse.monthValue + 1)
        } else {
            timeToParse.monthValue.toString()
        }
        return timeToParse.year.toString() + "-" + month + "-" + timeToParse.dayOfMonth
    }
}