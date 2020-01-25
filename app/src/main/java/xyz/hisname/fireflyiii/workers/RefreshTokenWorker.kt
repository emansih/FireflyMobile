package xyz.hisname.fireflyiii.workers

import android.accounts.AccountManager
import android.content.Context
import androidx.work.*
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.OAuthService
import xyz.hisname.fireflyiii.repository.models.auth.AuthModel

class RefreshTokenWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters)  {

    private val accManager by lazy { AuthenticatorManager(AccountManager.get(context)) }

    override suspend fun doWork(): Result {
        val networkCall: Response<AuthModel>?
        var workResult: Result
        try {
            networkCall = genericService?.create(OAuthService::class.java)?.getRefreshToken("refresh_token",
                    accManager.refreshToken, accManager.clientId,
                    accManager.secretKey)
            val authResponse = networkCall?.body()
            if (authResponse != null && networkCall.isSuccessful) {
                accManager.accessToken = authResponse.access_token
                accManager.refreshToken = authResponse.refresh_token
                accManager.tokenExpiry = authResponse.expires_in
                FireflyClient.destroyInstance()
                workResult = Result.success()
            } else {
                workResult = Result.retry()
            }
        } catch (exception: Exception) {
            workResult = Result.retry()
        }
        return workResult
    }
}