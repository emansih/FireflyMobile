package xyz.hisname.fireflyiii

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.data.remote.api.AccountsService
import xyz.hisname.fireflyiii.data.remote.api.OAuthService

class FireflyApiTestCase {

    companion object {
        private const val FIREFLY_BASEURL = "demo.firefly-iii.org"
    }

    @Before
    fun destroy() = RetrofitBuilder.destroyInstance()

    @Test
    fun testOAuth() {
        val auth = RetrofitBuilder.getClient(FIREFLY_BASEURL)?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithSlash() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithHttps() {
        val auth = RetrofitBuilder.getClient("https://$FIREFLY_BASEURL")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPathInBaseUrl() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/login")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPathInBaseUrlAndSlash() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/login/")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPathInBaseUrlAndHttps() {
        val auth = RetrofitBuilder.getClient("https://$FIREFLY_BASEURL/login")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPathInBaseUrlAndHttp() {
        val auth = RetrofitBuilder.getClient("http://$FIREFLY_BASEURL/login")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "http://$FIREFLY_BASEURL/login${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testAccount(){
        val auth = RetrofitBuilder.getClient(FIREFLY_BASEURL)?.create(AccountsService::class.java)?.getPaginatedAccountType(
                "asset", 1)
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithSlash(){
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/")?.create(AccountsService::class.java)?.getPaginatedAccountType(
                "asset", 1)
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithHttps(){
        val auth = RetrofitBuilder.getClient("https://$FIREFLY_BASEURL")?.create(AccountsService::class.java)?.getPaginatedAccountType(
                "asset", 1)
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithHttp(){
        val auth = RetrofitBuilder.getClient("http://$FIREFLY_BASEURL")?.create(AccountsService::class.java)?.getPaginatedAccountType(
                "asset", 1)
        assertEquals(auth?.request()?.url().toString(),
                "http://$FIREFLY_BASEURL${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithPathInBaseUrl() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/login")?.create(AccountsService::class.java)?.getPaginatedAccountType(
                "asset", 1)
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithPathInBaseUrlAndSlash() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/login/")?.create(AccountsService::class.java)?.getPaginatedAccountType(
                "asset", 1)
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }
}