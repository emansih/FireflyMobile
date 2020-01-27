package xyz.hisname.fireflyiii

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.hisname.fireflyiii.data.remote.nominatim.NominatimClient
import xyz.hisname.fireflyiii.data.remote.nominatim.api.SearchService

class NominatimClientTestCase {

    @After
    fun tearDown() {
        NominatimClient.destroyClient()
    }

    @Test
    fun searchIstana(){
        val nom = runBlocking{
            NominatimClient.getClient()?.create(SearchService::class.java)?.searchLocation("Istana")
        }
        assertTrue("list not empty", !nom.isNullOrEmpty())
        nom?.forEachIndexed { _, locationSearchModel ->
            if(locationSearchModel.display_name == "Istana, 1, Edinburgh Road, Robertson Quay, Selegie, Singapore, Central, 228091, Singapore"){
                assertEquals("historic",locationSearchModel.category)
                assertEquals(1.3071009, locationSearchModel.lat, 0.0)
                assertEquals(103.84291496937817, locationSearchModel.lon, 0.0)
            }
        }
    }

    @Test
    fun searchWithoutInput(){
        val nom = runBlocking{
            NominatimClient.getClient()?.create(SearchService::class.java)?.searchLocation("")
        }
        assertTrue("list is empty", nom.isNullOrEmpty())
    }


}