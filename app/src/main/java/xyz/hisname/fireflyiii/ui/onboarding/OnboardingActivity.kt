package xyz.hisname.fireflyiii.ui.onboarding

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import xyz.hisname.fireflyiii.R
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.AuthViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.util.extension.*
import java.util.concurrent.TimeUnit


class OnboardingActivity: AppCompatActivity() {

    private val fireflyUrl by lazy { sharedPref.getString("fireflyUrl","") ?: "" }
    private val fireflyId by lazy { sharedPref.getString("fireflyId","") ?: ""}
    private val fireflySecretKey by lazy { sharedPref.getString("fireflySecretKey","") ?: "" }
    private val fireflyAccessTokenExpiry by lazy { sharedPref.getString("expires_at","") ?: ""}
    private val userEmail by lazy { sharedPref.getString("userEmail","") ?: ""}
    private val userRole by lazy { sharedPref.getString("userRole", "") ?: "" }
    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private val model by lazy { getViewModel(AuthViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        when {
            fireflyUrl.isEmpty() or fireflyId.isEmpty() or fireflySecretKey.isEmpty() or
                    fireflyAccessTokenExpiry.isEmpty() -> {
                val bundle = bundleOf("ACTION" to "LOGIN")
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, LoginFragment().apply { arguments = bundle })
                        .commit()
            }
            System.currentTimeMillis() > fireflyAccessTokenExpiry.toLong() -> {
                val bundle = bundleOf("ACTION" to "REFRESH_TOKEN")
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, LoginFragment().apply { arguments = bundle })
                        .commit()
            }
            userEmail.isEmpty() or userRole.isEmpty() -> {
                val bundle = bundleOf("fireflyUrl" to fireflyUrl, "access_token" to sharedPref.getString("access_token",""))
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, OnboardingFragment().apply { arguments = bundle })
                        .commit()
            }
            else -> {
                val bundle = bundleOf("ACTION" to "HOME")
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, LoginFragment().apply { arguments = bundle })
                        .commit()
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
                        val bundle = bundleOf("fireflyUrl" to fireflyUrl, "access_token" to it.getAuth()?.access_token)
                        supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, OnboardingFragment().apply { arguments = bundle })
                                .commit()
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

