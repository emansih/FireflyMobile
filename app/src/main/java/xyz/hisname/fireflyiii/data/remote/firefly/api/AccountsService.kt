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
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.Constants.Companion.ACCOUNTS_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.accounts.AccountsModel
import xyz.hisname.fireflyiii.repository.models.accounts.AccountSuccessModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentModel
import xyz.hisname.fireflyiii.repository.models.transaction.TransactionModel

interface AccountsService {

    @GET(ACCOUNTS_API_ENDPOINT)
    suspend fun getPaginatedAccountType(@Query("type") type: String,
                                        @Query("page") page: Int): Response<AccountsModel>

    @FormUrlEncoded
    @POST(ACCOUNTS_API_ENDPOINT)
    suspend fun addAccount(@Field("name") name: String,
                   @Field("type") type: String,
                   @Field("currency_code") currencyCode: String?,
                   @Field("iban") iban: String?,
                   @Field("bic") bic: String?,
                   @Field("account_number") accountNumber: String?,
                   @Field("opening_balance") openingBalance: String?,
                   @Field("opening_balance_date") openingBalanceDate: String?,
                   @Field("account_role") accountRole: String?,
                   @Field("virtual_balance") virtualBalance: String?,
                   @Field("include_net_worth") includeNetWorth: Boolean,
                   @Field("notes") notes: String?,
                   @Field("liability_type") liabilityType: String?,
                   @Field("liability_amount") liabilityAmount: String?,
                   @Field("liability_start_date") liabilityStartDate: String?,
                   @Field("interest") interest: String?,
                   @Field("interest_period") interestPeriod: String?): Response<AccountSuccessModel>

    @FormUrlEncoded
    @PUT("$ACCOUNTS_API_ENDPOINT/{accountId}")
    suspend fun updateAccount(@Path("accountId") accountId: Long,
                      @Field("name") name: String,
                      @Field("type") type: String,
                      @Field("currency_code") currencyCode: String?,
                      @Field("iban") iban: String?,
                      @Field("bic") bic: String?,
                      @Field("account_number") accountNumber: String?,
                      @Field("opening_balance") openingBalance: String?,
                      @Field("opening_balance_date") openingBalanceDate: String?,
                      @Field("account_role") accountRole: String?,
                      @Field("virtual_balance") virtualBalance: String?,
                      @Field("include_net_worth") includeNetWorth: Boolean,
                      @Field("notes") notes: String?,
                      @Field("liability_type") liabilityType: String?,
                      @Field("liability_amount") liabilityAmount: String?,
                      @Field("liability_start_date") liabilityStartDate: String?,
                      @Field("interest") interest: String?,
                      @Field("interest_period") interestPeriod: String?): Response<AccountSuccessModel>

    @DELETE("$ACCOUNTS_API_ENDPOINT/{id}")
    suspend fun deleteAccountById(@Path("id") id: Long): Response<AccountsModel>

    @GET("${Constants.SEARCH_API_ENDPOINT}/accounts")
    suspend fun searchAccount(@Query("query") query: String,
                              @Query("type") type: String,
                              @Query("field") field: String = "name"): Response<AccountsModel>

    @GET("${Constants.SEARCH_API_ENDPOINT}/accounts/{id}/transactions")
    suspend fun getTransactionsByAccountId(@Path("id") id: Long,
                                           @Query("page") page: Int,
                                           @Query("start") startDate: String,
                                           @Query("end") endDate: String,
                                           @Query("type") type: String): Response<TransactionModel>

    @GET("$ACCOUNTS_API_ENDPOINT/{id}")
    suspend fun getAccountById(@Path("id") id: Long): Response<AccountsModel>

    @GET("${ACCOUNTS_API_ENDPOINT}/{id}/attachments")
    suspend fun getAccountAttachment(@Path("id") accountId: Long): Response<AttachmentModel>

}