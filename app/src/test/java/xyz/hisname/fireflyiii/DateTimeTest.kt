package xyz.hisname.fireflyiii

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import xyz.hisname.fireflyiii.util.DateTimeUtil

class DateTimeTest {

    @ParameterizedTest
    @ValueSource(strings = ["1593266400000", "1593267550000", "1593352799999", "1593320099999"])
    fun testCalToString(epochTime: String){
        val date = DateTimeUtil.getCalToString(epochTime)
        assertEquals("2020-06-28", date)
    }

    @Test
    fun testStartOfDay(){
        val date = DateTimeUtil.getStartOfDayInCalendarToEpoch("2020-06-28")
        assertEquals("1593266400000", date)
    }

    @Test
    fun testEndOfDay(){
        val date = DateTimeUtil.getEndOfDayInCalendarToEpoch("2020-06-28")
        assertEquals("1593352799000", date)
    }

    @DisplayName("Convert yyyy-MM-dd to mm YY")
    @Test
    fun testMonthYear(){
        val monthYear = DateTimeUtil.getMonthAndYear("2020-06-28")
        assertEquals("Jun 2020", monthYear)
    }

    @DisplayName("Convert yyyy-MM-dd to dd/MM")
    @Test
    fun testDayMonth(){
        val durationText = DateTimeUtil.getDayAndMonth("2020-06-28")
        assertEquals("28/6", durationText)
    }
}