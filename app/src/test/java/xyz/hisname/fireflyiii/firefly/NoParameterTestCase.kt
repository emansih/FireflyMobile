package xyz.hisname.fireflyiii.firefly

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.OAuthService
import java.util.concurrent.TimeUnit

// Test with **NO** parameters in the request URL
class NoParameterTestCase {

    companion object {
        private const val FIREFLY_BASEURL = "127.0.0.1"
    }

    private lateinit var mockServer: ClientAndServer
    private lateinit var mockServerClient: MockServerClient

    @BeforeEach
    fun setupTest(){
        mockServer = startClientAndServer(443, 8777, 1234)
        mockServerClient = MockServerClient(FIREFLY_BASEURL, 443)
        // Give some time for webserver to start up. It's required...really
        TimeUnit.SECONDS.sleep(3)
    }

    @AfterEach
    fun destroy() {
        FireflyClient.destroyInstance()
        mockServerClient.stop()
        mockServer.stop()
        // Give some time for webserver to shut down. It's required...really
        TimeUnit.SECONDS.sleep(3)
    }

    @Test
    fun testOAuth() {
        val auth = runBlocking {
            FireflyClient.getClient(FIREFLY_BASEURL, "", "",null, null)?.create(OAuthService::class.java)?.getAccessToken(
                    "", "", "", "", "")
        }
        assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPort() {
        val auth = runBlocking {
            FireflyClient.getClient("$FIREFLY_BASEURL:8777",
                    "", "", null, null)?.create(OAuthService::class.java)?.getAccessToken(
                    "", "", "", "", "")
        }
        assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL:8777/${Constants.OAUTH_API_ENDPOINT}/token")
    }


    @Test
    fun testOAuthWithSlash() {
        val auth = runBlocking {
            FireflyClient.getClient("$FIREFLY_BASEURL/", "",
                    "", null, null)?.create(OAuthService::class.java)?.getAccessToken(
                    "", "", "", "", "")
        }
        assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/${Constants.OAUTH_API_ENDPOINT}/token")

    }

    @Test
    fun testOAuthWithSlashAndPort() {
        val auth = runBlocking {
            FireflyClient.getClient("$FIREFLY_BASEURL:8777/","",
                    "", null, null)?.create(OAuthService::class.java)?.getAccessToken(
                    "", "", "", "", "")
        }
        assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL:8777/${Constants.OAUTH_API_ENDPOINT}/token")

    }

    @Test
    fun testOAuthWithHttps() {
        val auth = runBlocking {
            FireflyClient.getClient("https://$FIREFLY_BASEURL", "",
                    "", null, null)?.create(OAuthService::class.java)?.getAccessToken(
                    "", "", "", "", "")
        }
        assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPathInBaseUrl() {
        val auth = runBlocking {
            FireflyClient.getClient("$FIREFLY_BASEURL/login", "",
                    "", null, null)?.create(OAuthService::class.java)?.getAccessToken(
                    "", "", "", "", "")
        }
        assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPathAndPortInBaseUrl() {
        val auth = runBlocking {
            FireflyClient.getClient("$FIREFLY_BASEURL:1234/login", "",
                    "", null, null)?.create(OAuthService::class.java)?.getAccessToken(
                    "", "", "", "", "")
        }
        assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL:1234/login/${Constants.OAUTH_API_ENDPOINT}/token")

    }

    @Test
    fun testOAuthWithPathInBaseUrlAndSlash() {
        val auth = runBlocking {
            FireflyClient.getClient("$FIREFLY_BASEURL/login/", "",
                    "", null, null)?.create(OAuthService::class.java)?.getAccessToken(
                    "", "", "", "", "")
        }
        assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPathAndSlashAndPortInBaseUrl() {
        val auth = runBlocking {
            FireflyClient.getClient("$FIREFLY_BASEURL:1234/login/", "",
                    "", null, null)?.create(OAuthService::class.java)?.getAccessToken(
                    "", "", "", "", "")
        }
        assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL:1234/login/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPathInBaseUrlAndHttps() {
        val auth = runBlocking {
            FireflyClient.getClient("https://$FIREFLY_BASEURL/login", "",
                    "", null, null)?.create(OAuthService::class.java)?.getAccessToken(
                    "", "", "", "", "")
        }
        assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login/${Constants.OAUTH_API_ENDPOINT}/token")
    }
}