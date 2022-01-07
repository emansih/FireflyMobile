package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import xyz.hisname.fireflyiii.Constants.Companion.AUTOCOMPLETE_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.search.SearchModelItem

interface SearchService {

    @GET("${AUTOCOMPLETE_API_ENDPOINT}/budgets")
    suspend fun searchBudgets(@Query("query") queryString: String): Response<List<SearchModelItem>>

    @GET("${AUTOCOMPLETE_API_ENDPOINT}/categories")
    suspend fun searchCategories(@Query("query") queryString: String): Response<List<SearchModelItem>>

    @GET("${AUTOCOMPLETE_API_ENDPOINT}/tags")
    suspend fun searchTags(@Query("query") queryString: String): Response<List<SearchModelItem>>

    @GET("${AUTOCOMPLETE_API_ENDPOINT}/piggy-banks")
    suspend fun searchPiggyBanks(query: String): Response<List<SearchModelItem>>

    @GET("${AUTOCOMPLETE_API_ENDPOINT}/transactions")
    suspend fun searchTransactions(query: String): Response<List<SearchModelItem>>

    @GET("${AUTOCOMPLETE_API_ENDPOINT}/bills")
    suspend fun searchBills(query: String): Response<List<SearchModelItem>>

    @GET("${AUTOCOMPLETE_API_ENDPOINT}/currencies")
    suspend fun searchCurrencies(query: String): Response<List<SearchModelItem>>

    @GET("${AUTOCOMPLETE_API_ENDPOINT}/accounts")
    suspend fun searchAccounts(query: String): Response<List<SearchModelItem>>

}