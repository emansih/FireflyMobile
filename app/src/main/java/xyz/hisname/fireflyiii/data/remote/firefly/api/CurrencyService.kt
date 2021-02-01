/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.data.remote.firefly.api

import retrofit2.Response
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.CURRENCY_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyModel
import xyz.hisname.fireflyiii.repository.models.currency.CurrencySuccessModel
import xyz.hisname.fireflyiii.repository.models.currency.DefaultCurrencyModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionModel

interface CurrencyService {

    @GET(CURRENCY_API_ENDPOINT)
    suspend fun getPaginatedCurrency(@Query("page") page: Int): Response<CurrencyModel>

    @GET("$CURRENCY_API_ENDPOINT/default")
    suspend fun getDefaultCurrency(): Response<DefaultCurrencyModel>

    @FormUrlEncoded
    @POST(CURRENCY_API_ENDPOINT)
    suspend fun addCurrency(@Field("name") name: String,
                       @Field("code") code: String,
                       @Field("symbol") symbol: String,
                       @Field("decimal_places") decimalPlaces: String,
                       @Field("enabled") enabled: Boolean,
                       @Field("default") default: Boolean): Response<CurrencySuccessModel>

    @FormUrlEncoded
    @PUT("$CURRENCY_API_ENDPOINT/{currencyCode}")
    suspend fun updateCurrency(@Path("currencyCode") currencyCode: String,
                       @Field("name") name: String,
                       @Field("code") code: String,
                       @Field("symbol") symbol: String,
                       @Field("decimal_places") decimalPlaces: String,
                       @Field("enabled") enabled: Boolean,
                       @Field("default") default: Boolean): Response<CurrencySuccessModel>


    @DELETE("$CURRENCY_API_ENDPOINT/{currencyCode}")
    suspend fun deleteCurrencyByCode(@Path("currencyCode") currencyCode: String): Response<CurrencyModel>

    @GET("$CURRENCY_API_ENDPOINT/{currencyCode}")
    suspend fun getCurrencyByCode(@Query("currencyCode") currencyCode: String): Response<CurrencyModel>

    @GET("$CURRENCY_API_ENDPOINT/{code}")
    suspend fun getTransactionByCurrencyCode(@Path("code") currencyCode: String,
                                             @Query("page") page: Int,
                                             @Query("start_date") startDate: String,
                                             @Query("end_date") endDate: String,
                                             @Query("type") transactionType: String): Response<TransactionModel>

}