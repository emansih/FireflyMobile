package xyz.hisname.fireflyiii.ui.onboarding

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.AuthViewModel
import xyz.hisname.fireflyiii.ui.HomeActivity
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*
import java.util.concurrent.TimeUnit

class LoginFragment: Fragment() {

    private val baseUrl: String by lazy { sharedPref.getString("fireflyUrl","") ?: "" }
    private lateinit var fireflyUrl: String
    private val fireflyId: String by lazy { sharedPref.getString("fireflyId","") ?: "" }
    private val fireflySecretKey: String by lazy { sharedPref.getString("fireflySecretKey","") ?: "" }
    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private val model by lazy { getViewModel(AuthViewModel::class.java) }
    private val progressOverlay by lazy { requireActivity().findViewById<View>(R.id.progress_overlay) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_login, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val argument = arguments?.getString("ACTION")
        when {
            Objects.equals(argument, "LOGIN") -> {
                firefly_url_edittext.setText(baseUrl)
                firefly_id_edittext.setText(fireflyId)
                firefly_secret_edittext.setText(fireflySecretKey)
                getAccessCode()
            }
            Objects.equals(argument, "REFRESH_TOKEN") -> {
                refreshToken()
            }
        }
    }

    private fun getAccessCode(){
        firefly_submit_button.setOnClickListener {
            hideKeyboard()
            fireflyUrl = firefly_url_edittext.getString()
            val fireflyId = firefly_id_edittext.getString()
            val fireflySecretKey =  firefly_secret_edittext.getString()
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

    private fun refreshToken(){
        /* Bug: Currently there is a bug where if a user upgrades Firefly III, we have to request
            token again. Is it really a bug? Anyway, the client does not play well in this scenario.
            Currently we only checked if the refresh token is `old`
        */
        ProgressBar.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)
        model.getRefreshToken(baseUrl, sharedPref.getString("refresh_token", ""), fireflySecretKey)
                .observe(this, Observer {
                    if(it.getError() == null) {
                        val refreshtoken = it.getAuth()?.refresh_token ?: ""
                        sharedPref.edit {
                            putString("refresh_token", refreshtoken)
                            putString("access_token", it.getAuth()?.access_token)
                            putLong("expires_at", (System.currentTimeMillis() +
                                    TimeUnit.MINUTES.toMillis(it.getAuth()?.expires_in!!.toLong())))

                        }
                        startHomeIntent()
                    } else {
                        ProgressBar.animateView(progressOverlay, View.GONE, 0.toFloat(), 200)
                        val error = it.getError()
                        if(error!!.localizedMessage.startsWith("Unable to resolve host")){
                            toastInfo(resources.getString(R.string.unable_ping_server))
                        }
                    }
                })
    }

    private fun startHomeIntent(){
        if(sharedPref.getBoolean("persistent_notification",false)){
            NotificationUtils(requireContext()).showTransactionPersistentNotification()
        }
        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT, null, {
            delay(1234) //heh
            startActivity(Intent(requireContext(), HomeActivity::class.java))
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            requireActivity().finish()
        })
    }

    override fun onResume() {
        super.onResume()
        val uri = requireActivity().intent.data
        if(uri != null && uri.toString().startsWith(Constants.REDIRECT_URI)){
            val code = uri.getQueryParameter("code")
            if(code != null) {
                val sharedPref= PreferenceManager.getDefaultSharedPreferences(requireContext())
                val baseUrl= sharedPref.getString("fireflyUrl","") ?: ""
                val fireflyId = sharedPref.getString("fireflyId","") ?: ""
                val fireflySecretKey= sharedPref.getString("fireflySecretKey","") ?: ""
                ProgressBar.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)
                model.getAccessToken(baseUrl, code,fireflyId,fireflySecretKey).observe(this, Observer {
                    ProgressBar.animateView(progressOverlay, View.GONE, 0f, 200)
                    if(it.getAuth() != null) {
                        toastSuccess(resources.getString(R.string.welcome))
                        val refreshtoken = it.getAuth()?.refresh_token ?: ""
                        sharedPref.edit {
                            putString("refresh_token", refreshtoken)
                            putString("access_token", it.getAuth()?.access_token)
                            putLong("expires_at", (System.currentTimeMillis() +
                                    TimeUnit.MINUTES.toMillis(it.getAuth()!!.expires_in)))
                            putString("auth_method", "oauth")
                        }
                        val frameLayout = requireActivity().findViewById<FrameLayout>(R.id.bigger_fragment_container)
                        frameLayout.removeAllViews()
                        val bundle = bundleOf("fireflyUrl" to baseUrl, "access_token" to it.getAuth()?.access_token)
                        requireActivity().supportFragmentManager.beginTransaction()
                                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                .add(R.id.bigger_fragment_container, OnboardingFragment().apply { arguments = bundle })
                                .commit()
                    } else {
                        val error = it.getError()
                        if(error == null){
                            toastInfo("There was an error communicating with your server")
                        } else {
                            if (error.localizedMessage.startsWith("Unable to resolve host")) {
                                toastInfo(resources.getString(R.string.unable_ping_server))
                            } else {
                                toastInfo("There was an error communicating with your server")
                            }
                        }
                    }
                })
            } else {
                showDialog()
            }
        }
    }

    private fun showDialog(){
        ProgressBar.animateView(progressOverlay, View.GONE, 0.toFloat(), 200)
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.authentication_failed))
                .setMessage(resources.getString(R.string.authentication_failed_message, Constants.REDIRECT_URI))
                .setPositiveButton("OK") { _, _ ->
                }
                .create()
                .show()
    }
}