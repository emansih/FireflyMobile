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

import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.TemporalAdjusters.*
import java.time.temporal.WeekFields
import java.lang.Long.parseLong
import java.time.*
import java.time.format.DateTimeParseException
import java.util.*

object DateTimeUtil {

    fun getCalToString(date: String): String{
        return Instant.ofEpochMilli(parseLong(date))
                .atOffset(OffsetDateTime.now().offset)
                .toLocalDate()
                .toString()
    }

    fun convertLongToLocalDate(date: Long): LocalDate{
        return Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
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
        val todayDate = ZonedDateTime.now().toLocalDate()
        val localDate = ZonedDateTime.parse(date).toLocalDate()
        return Duration.between(todayDate.atStartOfDay(), localDate.atStartOfDay()).toDays().toString()
    }

    fun getDaysDifference(start: LocalDate, end: LocalDate): Long {
        return Duration.between(start.atStartOfDay(), end.atStartOfDay()).toDays()
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

    fun getFutureStartOfMonth(duration: Long): String {
        val localDateTime = LocalDate.now()
        val previousMonth = localDateTime.plusMonths(duration)
        val previousLocalDate =
                LocalDate.of(previousMonth.year, previousMonth.monthValue, previousMonth.dayOfMonth)
        return previousLocalDate.with(firstDayOfMonth()).toString()
    }

    fun getFutureEndOfMonth(duration: Long): String {
        val localDateTime = LocalDate.now()
        val previousMonth = localDateTime.plusMonths(duration)
        val previousLocalDate =
                LocalDate.of(previousMonth.year, previousMonth.monthValue, previousMonth.dayOfMonth)
        return previousLocalDate.withDayOfMonth(previousMonth.lengthOfMonth()).toString()
    }

    fun getStartOfMonth(): String{
        val localDateTime = LocalDate.now()
        val localDate = LocalDate.of(localDateTime.year, localDateTime.monthValue, localDateTime.dayOfMonth)
        return localDate.with(firstDayOfMonth()).toString()
    }

    fun getStartOfWeekFromGivenDate(date: String, number: Long): String{
        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return localDate.plusWeeks(number).toString()
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


    fun getPreviousMonthShortName(duration: Long): String{
        val localDateTime = LocalDate.now()
        return localDateTime.minusMonths(duration).month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    fun getTodayDate(): String{
        return LocalDate.now().toString()
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


    /* Input LocalDateTime(yyyy-MM-dd T HH:mm)
     * 0 -> dd MM yyyy hh:mm a   (08:30am)
     * 1 -> dd MM yyyy HH:mm (15:30)
     * 2 -> MM dd yyyy hh:mm a
     * 3 -> MM dd yyyy HH:mm
     * 4 -> dd MMM yyyy hh:mm a
     * 5 -> dd MMM yyyy HH:mm
     * 6 -> MMM dd yyyy hh:mm a
     * 7 -> MMM dd yyyy HH:mm
     */
    fun convertLocalDateTime(timeToParse: OffsetDateTime, dateTimeFormatPref: Int,
                             customDateTimeFormat: String): String {
        val timeFormat: String
        if(customDateTimeFormat.isBlank()){
            timeFormat = when (dateTimeFormatPref) {
                0 -> {
                    timeToParse.format(DateTimeFormatter.ofPattern("dd MM yyyy hh:mm a"))
                }
                1 -> {
                    timeToParse.format(DateTimeFormatter.ofPattern("dd MM yyyy HH:mm"))
                }
                2 -> {
                    timeToParse.format(DateTimeFormatter.ofPattern("MM dd yyyy hh:mm a"))
                }
                3 -> {
                    timeToParse.format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a"))
                }
                4 -> {
                    timeToParse.format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a"))
                }
                5 -> {
                    timeToParse.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
                }
                6 -> {
                    timeToParse.format(DateTimeFormatter.ofPattern("MMM dd yyyy hh:mm a"))
                }
                else -> {
                    timeToParse.format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm"))
                }
            }
        } else {
            timeFormat = try {
                timeToParse.format(DateTimeFormatter.ofPattern(customDateTimeFormat))
            } catch (exception: Exception){
                timeToParse.format(DateTimeFormatter.ofPattern("dd MM yyyy hh:mm a"))
            }
        }

        return timeFormat
    }

    fun convertIso8601ToHumanDate(timeToParse: OffsetDateTime): String {
        val month = if (timeToParse.monthValue < 10){
             "0${timeToParse.monthValue}"
        } else {
            timeToParse.monthValue.toString()
        }
        val dayOfMonth = if(timeToParse.dayOfMonth < 10){
            "0${timeToParse.dayOfMonth}"
        } else {
            timeToParse.dayOfMonth.toString()
        }
        return timeToParse.year.toString() + "-" + month + "-" + dayOfMonth
    }

    fun convertIso8601ToHumanTime(timeToParse: OffsetDateTime): String{
        val min = if(timeToParse.minute < 10){
            "0${timeToParse.minute}"
        } else {
            timeToParse.minute.toString()
        }
        return "${timeToParse.hour}:${min}"
    }

    fun offsetDateTimeWithoutTime(date: String?) = date + "T00:00:00" + ZonedDateTime.now().offset


    fun mergeDateTimeToIso8601(date: String, time: String) =  date + "T" + time + ZonedDateTime.now().offset

    fun convertEpochToHumanTime(dateToConvert: Long): String{
        val date =
                Instant.ofEpochMilli(dateToConvert).atZone(ZoneId.systemDefault()).toLocalDate()

        return date.dayOfMonth.toString() + " " +
                date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " +
                date.year + " @ " + convertIso8601ToHumanTime(date.atTime(OffsetTime.now()))
    }

}