package xyz.hisname.fireflyiii.repository.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import xyz.hisname.fireflyiii.Constants.Companion.ACCOUNTS_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.accounts.AccountsModel

// Link to relevant doc: https://firefly-iii.readthedocs.io/en/latest/api/accounts.html
interface AccountsService {

    @GET(ACCOUNTS_API_ENDPOINT)
    fun getAccountType(@Query("type") type: String): Call<AccountsModel>
}