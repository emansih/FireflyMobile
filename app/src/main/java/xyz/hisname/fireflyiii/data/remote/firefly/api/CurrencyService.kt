package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.CURRENCY_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyModel
import xyz.hisname.fireflyiii.repository.models.currency.CurrencySuccessModel
import xyz.hisname.fireflyiii.repository.models.currency.DefaultCurrencyModel

// Link to relevant doc: https://firefly-iii.readthedocs.io/en/latest/api/currency.html
interface CurrencyService {

    @GET(CURRENCY_API_ENDPOINT)
    suspend fun getSuspendedPaginatedCurrency(@Query("page") page: Int): Response<CurrencyModel>

    @GET("$CURRENCY_API_ENDPOINT/default")
    suspend fun getDefaultCurrency(): Response<DefaultCurrencyModel>

    @GET("$CURRENCY_API_ENDPOINT/{id}")
    fun getCurrencyById(@Path("id") id: String)

    @FormUrlEncoded
    @POST(CURRENCY_API_ENDPOINT)
    fun createCurrency(@Field("name") name: String,
                       @Field("code") code: String,
                       @Field("symbol") symbol: String,
                       @Field("decimal_places") decimalPlaces: String,
                       @Field("enabled") enabled: Boolean,
                       @Field("default") default: Boolean): Call<CurrencySuccessModel>

    @FormUrlEncoded
    @PUT("$CURRENCY_API_ENDPOINT/{currencyCode}")
    fun updateCurrency(@Path("currencyCode") currencyCode: String,
                       @Field("name") name: String,
                       @Field("code") code: String,
                       @Field("symbol") symbol: String,
                       @Field("decimal_places") decimalPlaces: String,
                       @Field("enabled") enabled: Boolean,
                       @Field("default") default: Boolean): Call<CurrencySuccessModel>


    @DELETE("$CURRENCY_API_ENDPOINT/{currencyCode}")
    suspend fun deleteCurrencyByCode(@Path("currencyCode") currencyCode: String): Response<CurrencyModel>

}