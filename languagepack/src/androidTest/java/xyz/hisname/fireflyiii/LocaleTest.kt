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

package xyz.hisname.fireflyiii

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import xyz.hisname.languagepack.LanguageChanger
import xyz.hisname.languagepack.R

@RunWith(AndroidJUnit4ClassRunner ::class)
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

    @Test
    fun testSupportedLocale_SimplifiedChinese(){
        val contextWrapper = LanguageChanger.init(appContext, "zh-rCN")
        assertEquals("监控面板", contextWrapper.resources.getString(R.string.dashboard))
    }

    @Test
    fun testSupportedLocale_TraditionalChinese(){
        val contextWrapper = LanguageChanger.init(appContext, "zh-rTW")
        assertEquals("監控面板", contextWrapper.resources.getString(R.string.dashboard))
    }

    @Test
    fun testSupportedLocale_Dutch(){
        val contextWrapper = LanguageChanger.init(appContext, "nl")
        assertEquals("Dashboard", contextWrapper.resources.getString(R.string.dashboard))
    }

    @Test
    fun testSupportedLocale_French(){
        val contextWrapper = LanguageChanger.init(appContext, "fr")
        assertEquals("Tableau de Bord", contextWrapper.resources.getString(R.string.dashboard))
    }

    @Test
    fun testSupportedLocale_Italian(){
        val contextWrapper = LanguageChanger.init(appContext, "it")
        assertEquals("Cruscotto", contextWrapper.resources.getString(R.string.dashboard))
    }

    @Test
    fun testSupportedLocale_Russian(){
        val contextWrapper = LanguageChanger.init(appContext, "ru")
        assertEquals("Сводка", contextWrapper.resources.getString(R.string.dashboard))
    }

    @Test
    fun testSupportedLocale_Spanish(){
        val contextWrapper = LanguageChanger.init(appContext, "es")
        assertEquals("Panel de control", contextWrapper.resources.getString(R.string.dashboard))
    }
}
