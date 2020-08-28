package xyz.hisname.fireflyiii.fireflydao

import androidx.preference.PreferenceManager
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.NetworkType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import xyz.hisname.fireflyiii.data.local.pref.AppPref

@RunWith(AndroidJUnit4ClassRunner ::class)
class AppPrefTest {

    private lateinit var appPref: AppPref

    @Before
    fun initSharedPref() {
        appPref = AppPref(PreferenceManager
                .getDefaultSharedPreferences(InstrumentationRegistry.getInstrumentation().context))
    }

    @Test
    fun testInvalidDelayWorkManager(){
        appPref.workManagerDelay = 5L
        assertTrue(appPref.workManagerDelay != 5L)
        assertEquals(15L, appPref.workManagerDelay)
    }

    @Test
    fun testValidDelayWorkManager(){
        appPref.workManagerDelay = 20L
        assertTrue(appPref.workManagerDelay == 20L)
    }

    @Test
    fun testConnectedWorkManager(){
        appPref.workManagerNetworkType = NetworkType.CONNECTED
        assertTrue(appPref.workManagerNetworkType == NetworkType.CONNECTED)
    }

    @Test
    fun testNotRoamingWorkManager(){
        appPref.workManagerNetworkType = NetworkType.NOT_ROAMING
        assertTrue(appPref.workManagerNetworkType == NetworkType.NOT_ROAMING)
    }

    @Test
    fun testUnmeteredWorkManager(){
        appPref.workManagerNetworkType = NetworkType.UNMETERED
        assertTrue(appPref.workManagerNetworkType == NetworkType.UNMETERED)
    }
}