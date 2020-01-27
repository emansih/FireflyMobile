package xyz.hisname.fireflyiii.firefly

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import java.util.concurrent.TimeUnit

// Test with parameters in the request URL
class ParameterTestCase {

    companion object {
        private const val FIREFLY_BASEURL = "127.0.0.1"
    }

    private lateinit var mockServer: ClientAndServer
    private lateinit var mockServerClient: MockServerClient

    @Before
    fun setupTest(){
        mockServer = ClientAndServer.startClientAndServer(443, 8777, 1234)
        mockServerClient = MockServerClient(FIREFLY_BASEURL, 443)
        // Give some time for webserver to start up. It's required...really
        // 3 IS the magic number.
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
    fun testAccount(){
        val auth = runBlocking {
            FireflyClient.getClient(FIREFLY_BASEURL, "",
                    "", null, null)?.create(AccountsService::class.java)?.getPaginatedAccountType(
                    "asset", 1)
        }
        Assert.assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithSlash(){
        val auth = runBlocking {
            FireflyClient.getClient("$FIREFLY_BASEURL/", "",
                    "", null, null)?.create(AccountsService::class.java)?.getPaginatedAccountType(
                    "asset", 1)
        }
        Assert.assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithHttps(){
        val auth = runBlocking {
            FireflyClient.getClient("https://$FIREFLY_BASEURL", "",
                    "", null, null)?.create(AccountsService::class.java)?.getPaginatedAccountType(
                    "asset", 1)
        }
        Assert.assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithPathInBaseUrl() {
        val auth = runBlocking {
            FireflyClient.getClient("$FIREFLY_BASEURL/login", "",
                    "", null, null)?.create(AccountsService::class.java)?.getPaginatedAccountType(
                    "asset", 1)
        }
        Assert.assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login/${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithPathInBaseUrlAndSlash() {
        val auth = runBlocking {
            FireflyClient.getClient("$FIREFLY_BASEURL/login/", "",
                    "", null, null)?.create(AccountsService::class.java)?.getPaginatedAccountType(
                    "asset", 1)
        }
        Assert.assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login/${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithPathAndSlashAndPortInBaseUrl() {
        val auth = runBlocking {
            FireflyClient.getClient("$FIREFLY_BASEURL:1234/login/", "",
                    "", null, null)?.create(AccountsService::class.java)?.getPaginatedAccountType(
                    "asset", 1)
        }
        Assert.assertEquals(auth?.raw()?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL:1234/login/${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }
}