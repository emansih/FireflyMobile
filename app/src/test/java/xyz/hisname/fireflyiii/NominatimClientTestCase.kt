package xyz.hisname.fireflyiii

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import xyz.hisname.fireflyiii.data.remote.nominatim.NominatimClient
import xyz.hisname.fireflyiii.data.remote.nominatim.api.SearchService

class NominatimClientTestCase {

    @AfterEach
    fun tearDown() {
        NominatimClient.destroyClient()
    }

    @Test
    fun searchIstana(){
        val nom = runBlocking{
            NominatimClient.getClient()?.create(SearchService::class.java)?.searchLocation("Istana")
        }
        assertTrue(!nom.isNullOrEmpty(), "list not empty")
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
        assertTrue(nom.isNullOrEmpty(), "list is empty")
    }


}