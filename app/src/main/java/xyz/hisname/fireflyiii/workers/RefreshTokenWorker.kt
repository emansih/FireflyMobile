package xyz.hisname.fireflyiii.workers

import android.content.Context
import androidx.work.*
import retrofit2.Call
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.data.remote.api.OAuthService
import xyz.hisname.fireflyiii.repository.models.auth.AuthModel

class RefreshTokenWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters)  {

    override fun doWork(): Result {
        return if(service()?.execute()?.isSuccessful!!){
            Result.SUCCESS
        } else {
            Result.RETRY
        }
    }

    private fun service(): Call<AuthModel>?{
        var cert = ""
        if(AppPref(context).enableCertPinning){
            cert = AppPref(context).certValue
        }
        return RetrofitBuilder.getClient(AppPref(context).baseUrl,
                AppPref(context).accessToken, cert)?.create(OAuthService::class.java)?.getRefreshToken("refresh_token",
                AppPref(context).refreshToken, AppPref(context).clientId,
                AppPref(context).secretKey)
    }
}