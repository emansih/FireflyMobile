package xyz.hisname.fireflyiii.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_login.*
import xyz.hisname.fireflyiii.R
import kotlinx.android.synthetic.main.progress_overlay.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.android.UI
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.AuthViewModel
import xyz.hisname.fireflyiii.util.extension.getString
import xyz.hisname.fireflyiii.util.extension.toastError
import xyz.hisname.fireflyiii.util.extension.toastInfo
import xyz.hisname.fireflyiii.util.extension.toastSuccess
import java.util.concurrent.TimeUnit


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class LoginActivity: AppCompatActivity() {

    private lateinit var fireflyUrl: String
    private lateinit var fireflyId: String
    private lateinit var fireflySecretKey: String
    private lateinit var fireflyAccessTokenExpiry: String
    private lateinit var sharedPref: SharedPreferences
    private lateinit var model: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        fireflyUrl = sharedPref.getString("fireflyUrl", "")
        fireflyId = sharedPref.getString("fireflyId", "")
        fireflySecretKey = sharedPref.getString("fireflySecretKey", "")
        fireflyAccessTokenExpiry = sharedPref.getString("expires_at", "")
        model = ViewModelProviders.of(this)[AuthViewModel::class.java]
        if(fireflyUrl.isEmpty() or fireflyId.isEmpty() or fireflySecretKey.isEmpty() or
                fireflyAccessTokenExpiry.isEmpty()){
            // User has not signed in yet
            user_details_layout.isVisible = true
            getAccessCode()
        } else {
            /* Bug: Currently there is a bug where if a user upgrades Firefly III, we have to request
            token again. Is it really a bug? Anyway, the client does not play well in this scenario.
            Currently we only checked if the refresh token is `old`
            */
            if(System.currentTimeMillis() > fireflyAccessTokenExpiry.toLong()){
                ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
                // Token expired. Refreshing now...
                model.getRefreshToken(fireflyUrl, sharedPref.getString("refresh_token", ""), fireflySecretKey)
                        .observe(this, Observer {
                            if(it.getError() == null) {
                                run {
                                    sharedPref.edit {
                                        putString("refresh_token", it.getAuth()?.refresh_token)
                                        putString("access_token", it.getAuth()?.access_token)
                                        // Dirty hack for now...
                                        // TODO: use `putLong()` method instead of `putString()`
                                        putString("expires_at", (System.currentTimeMillis() +
                                                TimeUnit.MINUTES.toMillis(it.getAuth()?.expires_in!!.toLong())).toString())
                                    }
                                }
                            } else {
                                ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
                                val error = it.getError()
                                if(error!!.localizedMessage.startsWith("Unable to resolve host")){
                                    toastInfo(resources.getString(R.string.unable_ping_server))
                                }
                            }
                        })
               startHomeIntent()
            } else {
              startHomeIntent()
            }
        }
    }

    private fun startHomeIntent(){
        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT, null, {
            delay(1234) //heh
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        })
    }

    private fun getAccessCode(){
        firefly_submit_button.setOnClickListener {
            fireflyUrl = firefly_url_edittext.getString()
            fireflyId = firefly_id_edittext.getString()
            fireflySecretKey =  firefly_secret_edittext.getString()
            if(fireflyUrl.isEmpty() or fireflyId.isEmpty() or fireflySecretKey.isEmpty()){
                when {
                    fireflyUrl.isEmpty() -> firefly_url_edittext.error = resources.getString(R.string.required_field)
                    fireflyId.isEmpty() -> firefly_id_edittext.error = resources.getString(R.string.required_field)
                    else -> firefly_secret_edittext.error = resources.getString(R.string.required_field)
                }
            } else {
                // Since user didn't bother to add http/https prefix, we will do it for them...
                if(!fireflyUrl.startsWith("http")){
                    fireflyUrl = "https://$fireflyUrl"
                }
                sharedPref.edit {
                    putString("fireflyUrl",fireflyUrl)
                    putString("fireflyId",fireflyId)
                    putString("fireflySecretKey",fireflySecretKey)
                }
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = ("$fireflyUrl/oauth/authorize?client_id=$fireflyId" +
                            "&redirect_uri=${Constants.REDIRECT_URI}&scope=&response_type=code&state=").toUri()
                    startActivity(intent)
                } catch (exception: ActivityNotFoundException){
                    // this user doesn't have a browser installed on their device?!
                    toastError(resources.getString(R.string.no_browser_installed))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val uri = intent.data
        if(uri != null && uri.toString().startsWith(Constants.REDIRECT_URI)){
            val code = uri.getQueryParameter("code")
            if(code != null) {
                ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
                model.getAccessToken(fireflyUrl,code,fireflyId,fireflySecretKey).observe(this, Observer {
                    if(it.getError() == null) {
                        toastSuccess(resources.getString(R.string.welcome))
                        sharedPref.edit {
                            putString("refresh_token", it.getAuth()?.refresh_token)
                            putString("access_token", it.getAuth()?.access_token)
                            // Dirty hack for now...
                            // TODO: use `putLong()` instead of `putString()`
                            putString("expires_at", (System.currentTimeMillis() +
                                    TimeUnit.MINUTES.toMillis(it.getAuth()?.expires_in!!.toLong())).toString())
                        }
                        startHomeIntent()
                    } else {
                        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
                        val error = it.getError()
                        if (error!!.localizedMessage.startsWith("Unable to resolve host")) {
                            toastInfo(resources.getString(R.string.unable_ping_server))
                        }
                    }
                })
            } else {
               showDialog()
            }
        }
    }

    private fun showDialog(){
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.authentication_failed))
                .setMessage(resources.getString(R.string.authentication_failed_message, Constants.REDIRECT_URI))
                .setPositiveButton("OK") { _, _ ->
                    sharedPref.edit {
                        putString("fireflyUrl","")
                        putString("fireflyId","")
                        putString("fireflySecretKey","")
                    }
                }
                .create()
                .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        RetrofitBuilder.destroyInstance()
    }
}

