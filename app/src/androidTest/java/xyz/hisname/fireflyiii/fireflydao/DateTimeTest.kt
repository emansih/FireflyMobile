package xyz.hisname.fireflyiii.fireflydao

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import xyz.hisname.fireflyiii.util.DateTimeUtil

@RunWith(AndroidJUnit4ClassRunner ::class)
class DateTimeTest {

    // TODO: These tests should not be tied to the Android Framework. Move them out
    @Before
    fun init(){
        AndroidThreeTen.init(InstrumentationRegistry.getInstrumentation().context)
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
}