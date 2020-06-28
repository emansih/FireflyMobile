package xyz.hisname.fireflyiii

import junit.framework.TestCase.assertEquals
import org.junit.Test
import xyz.hisname.fireflyiii.util.DateTimeUtil

class DateTimeTest {

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
}