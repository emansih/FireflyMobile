package xyz.hisname.fireflyiii.firefly

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.OAuthService
import xyz.hisname.fireflyiii.repository.models.auth.AuthModel
import java.util.concurrent.TimeUnit

class RequestBodyTestCase {

    companion object {
        private const val FIREFLY_BASEURL = "127.0.0.1"
    }

    private lateinit var mockServer: ClientAndServer
    private lateinit var mockServerClient: MockServerClient
    private val bearerToken by lazy { "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImRlOGU3YzIzNjhkYTJkNTEwNTQzOWI5MzhkODhjOTFkMmY1MGRlZDhlNTgwMWFkMzMyMjJlNmFkYjIxYWU0OTRkYzUyOGY3MDJjYTQwNzU3In0.eyJhdWQiOiIxIiwianRpIjoiZGU4ZTdjMjM2OGRhMmQ1MTA1NDM5YjkzOGQ4OGM5MWQyZjUwZGVkOGU1ODAxYWQzMzIyMmU2YWRiMjFhZTQ5NGRjNTI4ZjcwMmNhNDA3NTciLCJpYXQiOjE1NTUxNzA2NjcsIm5iZiI6MTU1NTE3MDY2NywiZXhwIjoxNTg2NzkzMDY3LCJzdWIiOiIxIiwic2NvcGVzIjpbXX0.ZDVDJBYo5utRIaDDKqNHN6KYSinpHQAp-8LbUl0SJrf54W3JOL1kJafLBDy_Psx66vwqw1wUHnspUIPSgTnUD9_jyfGonVuf45lZ_Z2O7HF_amwYE27eDtMZKxis48cFhcJOMBjMYO2jRwkaZPhQSTyIM7DdeKBWTftykkm9nhF0-BylFCf2vMqLJgOekOLLOjlgp8-a4HBgQCjfz9nmcMxnwRv_AYu1-TSNqFwYBkH1yfK9JkkrLirgOijbgj6sOE0Ut98iTvE-ORWhmgAVoNjA7CarXGXQfI6lbBOZ8-bncpCO78XKCEljXirJ7dHG6vvFSQVYTqKEctkp8w9YO8Z-u7dmPWZx7KBV26Q092q70x6rA4wfoRTEkAWUC052NsCPWv3YdWqOIk4Df5S8XjrfiCkxjBR-9U-PU8BSLl3xtIjcf34JUzU-_DpGQx__Wlc1ibwgJInmI-XhGbDCryqJG5vNSZCY9Axlnn5MLpQbGMiJYf_qxTskv0uO0lOYmrKJXsu7gIn7uYX25axAnciYSxYomiWn1jbPHJcQ_KdUvrgqpGJAopbQHepSSXjBBSuo_nDzupmRaud6H4HXrJjRU1yS8qpAFc8FaibBwqNyZTVEI0dFDDxlYqStheYiy_AFeNGzpYo4zOrMalgiDpLT5JzP7Ut4AXGc5crmCz4" }

    @Before
    fun setupTest(){
        mockServer = ClientAndServer.startClientAndServer(443)
        mockServerClient = MockServerClient(FIREFLY_BASEURL, 443)
        mockServerClient.`when`(request()
                .withMethod("POST")
                .withPath("${Constants.OAUTH_API_ENDPOINT}/token"))
                .respond(HttpResponse.response()
                        .withBody(AuthModel("", 1234L, "","").toString())
                )

        // Give some time for webserver to start up. It's required...really
        TimeUnit.SECONDS.sleep(3)
    }

    @After
    fun destroy() {
        FireflyClient.destroyInstance()
        mockServerClient.stop()
        mockServer.stop()
        // Give some time for webserver to shut down. It's required...really
        TimeUnit.SECONDS.sleep(3)
    }

    @Test
    fun testRequestHeader(){
        runBlocking {
            FireflyClient.getClient(FIREFLY_BASEURL, "", "",null, null)?.create(OAuthService::class.java)?.getAccessToken(
                    "", "", "", "")
        }
        val requestHeader = mockServerClient.retrieveRecordedRequests(request())[0].headers
        assertTrue("header not empty", !requestHeader.isEmpty)
        assertEquals(requestHeader.getValues("User-Agent")[0],BuildConfig.APPLICATION_ID)
        assertEquals(requestHeader.getValues("Accept")[0], "application/json")
    }


    @Test
    fun testRequestBody(){
        runBlocking {
            FireflyClient.getClient(FIREFLY_BASEURL, "", "",null, null)?.create(OAuthService::class.java)?.getAccessToken(
                    bearerToken, "1", "tfWoJQbmV88Fxej1ysoooFireflyIIIDemoToken", Constants.REDIRECT_URI)
        }
        val requestBody = mockServerClient.retrieveRecordedRequests(request())[0].body
        assertTrue("body not empty", !requestBody.value.toString().isBlank())
        assertEquals(bearerToken, requestBody)
    }
}