package xyz.hisname.fireflyiii

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.data.remote.api.AccountsService
import xyz.hisname.fireflyiii.data.remote.api.CurrencyService
import xyz.hisname.fireflyiii.data.remote.api.OAuthService
import xyz.hisname.fireflyiii.data.remote.api.TransactionService

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
                "https://$FIREFLY_BASEURL/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPort() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL:8777")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL:8777/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithSlash() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithSlashAndPort() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL:8777/")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL:8777/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithHttps() {
        val auth = RetrofitBuilder.getClient("https://$FIREFLY_BASEURL")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPathInBaseUrl() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/login")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPathAndPortInBaseUrl() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL:1234/login")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL:1234/login/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPathInBaseUrlAndSlash() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/login/")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPathAndSlashAndPortInBaseUrl() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL:1234/login/")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL:1234/login/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPathInBaseUrlAndHttps() {
        val auth = RetrofitBuilder.getClient("https://$FIREFLY_BASEURL/login")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testOAuthWithPathInBaseUrlAndHttp() {
        val auth = RetrofitBuilder.getClient("http://$FIREFLY_BASEURL/login")?.create(OAuthService::class.java)?.getAccessToken(
                "", "", "", "", "")
        assertEquals(auth?.request()?.url().toString(),
                "http://$FIREFLY_BASEURL/login/${Constants.OAUTH_API_ENDPOINT}/token")
    }

    @Test
    fun testAccount(){
        val auth = RetrofitBuilder.getClient(FIREFLY_BASEURL)?.create(AccountsService::class.java)?.getPaginatedAccountType(
                "asset", 1)
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithSlash(){
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/")?.create(AccountsService::class.java)?.getPaginatedAccountType(
                "asset", 1)
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithHttps(){
        val auth = RetrofitBuilder.getClient("https://$FIREFLY_BASEURL")?.create(AccountsService::class.java)?.getPaginatedAccountType(
                "asset", 1)
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithHttp(){
        val auth = RetrofitBuilder.getClient("http://$FIREFLY_BASEURL")?.create(AccountsService::class.java)?.getPaginatedAccountType(
                "asset", 1)
        assertEquals(auth?.request()?.url().toString(),
                "http://$FIREFLY_BASEURL/${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithPathInBaseUrl() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/login")?.create(AccountsService::class.java)?.getPaginatedAccountType(
                "asset", 1)
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login/${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithPathInBaseUrlAndSlash() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/login/")?.create(AccountsService::class.java)?.getPaginatedAccountType(
                "asset", 1)
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login/${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testAccountWithPathAndSlashAndPortInBaseUrl() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL:1234/login/")?.create(AccountsService::class.java)?.getPaginatedAccountType(
                "asset", 1)
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL:1234/login/${Constants.ACCOUNTS_API_ENDPOINT}?type=asset&page=1")
    }

    @Test
    fun testCurrency(){
        val auth = RetrofitBuilder.getClient(FIREFLY_BASEURL)?.create(CurrencyService::class.java)?.getPaginatedCurrency(
                1)
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/${Constants.CURRENCY_API_ENDPOINT}?page=1")

    }

    @Test
    fun testCurrencyWithPathInBaseUrl() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/login")?.create(CurrencyService::class.java)?.getPaginatedCurrency(
                1)
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login/${Constants.CURRENCY_API_ENDPOINT}?page=1")
    }

    @Test
    fun testCurrencyWithPathInBaseUrlAndSlash() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/login/")?.create(CurrencyService::class.java)?.getPaginatedCurrency(
                1)
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login/${Constants.CURRENCY_API_ENDPOINT}?page=1")
    }

    @Test
    fun testTransactionWithPathInBaseUrl() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/login")?.create(TransactionService::class.java)?.getAllTransactions(
                "2019-01-01", "2019-12-12", "asset")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login/${Constants.TRANSACTION_API_ENDPOINT}?start=2019-01-01&end=2019-12-12&type=asset")
    }

    @Test
    fun testTransactionWithPathInBaseUrlAndSlash() {
        val auth = RetrofitBuilder.getClient("$FIREFLY_BASEURL/login/")?.create(TransactionService::class.java)?.getAllTransactions(
                "2019-01-01", "2019-12-12", "asset")
        assertEquals(auth?.request()?.url().toString(),
                "https://$FIREFLY_BASEURL/login/${Constants.TRANSACTION_API_ENDPOINT}?start=2019-01-01&end=2019-12-12&type=asset")
    }
}