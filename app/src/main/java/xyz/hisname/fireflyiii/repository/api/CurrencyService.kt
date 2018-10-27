package xyz.hisname.fireflyiii.repository.api

import retrofit2.Call
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.CURRENCY_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyModel

// Link to relevant doc: https://firefly-iii.readthedocs.io/en/latest/api/currency.html
interface CurrencyService {

    // TODO add callbacks

    @GET(CURRENCY_API_ENDPOINT)
    fun getCurrency(): Call<CurrencyModel>

    @GET("$CURRENCY_API_ENDPOINT/{id}")
    fun getCurrencyById(@Path("id") id: String)

    @FormUrlEncoded
    @POST(CURRENCY_API_ENDPOINT)
    fun createCurrency(@Field("name") name: String, @Field("code") code: String,
                       @Field("symbol") symbol: String, @Field("decimal_places") decimalPlaces: String,
                       @Field("default") default: Boolean)

    @DELETE("$CURRENCY_API_ENDPOINT/{id}")
    fun deleteCurrencyById(@Path("id") id: String)

}