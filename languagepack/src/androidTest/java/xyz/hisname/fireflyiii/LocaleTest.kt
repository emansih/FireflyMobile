package xyz.hisname.fireflyiii

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import xyz.hisname.languagepack.LanguageChanger
import xyz.hisname.languagepack.R


class LocaleTest {

    private val appContext by lazy { InstrumentationRegistry.getInstrumentation().context }


    @Test
    fun testUnsupportedLocale_Polish(){
        val contextWrapper = LanguageChanger.init(appContext, "pl")
        assertEquals("Dashboard", contextWrapper.resources.getString(R.string.dashboard))
    }

    @Test
    fun testSupportedLocale_German(){
        val contextWrapper = LanguageChanger.init(appContext, "de")
        assertEquals("Übersicht", contextWrapper.resources.getString(R.string.dashboard))
    }

    @Test
    fun testSupportedLocale_English(){
        val contextWrapper = LanguageChanger.init(appContext, "en")
        assertEquals("Dashboard", contextWrapper.resources.getString(R.string.dashboard))
    }
}
