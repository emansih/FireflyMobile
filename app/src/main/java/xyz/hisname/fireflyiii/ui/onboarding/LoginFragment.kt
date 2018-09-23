package xyz.hisname.fireflyiii.ui.onboarding

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.progress_overlay.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.AuthViewModel
import xyz.hisname.fireflyiii.ui.HomeActivity
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*
import java.util.concurrent.TimeUnit

class LoginFragment: Fragment() {

    private lateinit var fireflyUrl: String
    private lateinit var fireflyId: String
    private lateinit var fireflySecretKey: String
    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private val model by lazy { getViewModel(AuthViewModel::class.java) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_login, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val argument = arguments?.getString("ACTION")
        when {
            Objects.equals(argument, "LOGIN") -> {
                user_details_layout.isVisible = true
                getAccessCode()
            }
            Objects.equals(argument, "REFRESH_TOKEN") -> {
                refreshToken()
            }
            Objects.equals(argument, "HOME") -> {
                startHomeIntent()
            }
        }
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

    private fun refreshToken(){
        /* Bug: Currently there is a bug where if a user upgrades Firefly III, we have to request
            token again. Is it really a bug? Anyway, the client does not play well in this scenario.
            Currently we only checked if the refresh token is `old`
        */
        ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
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
                        startHomeIntent()
                    } else {
                        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
                        val error = it.getError()
                        if(error!!.localizedMessage.startsWith("Unable to resolve host")){
                            toastInfo(resources.getString(R.string.unable_ping_server))
                        }
                    }
                })
    }

    private fun startHomeIntent(){
        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT, null, {
            delay(1234) //heh
            startActivity(Intent(requireContext(), HomeActivity::class.java))
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            requireActivity().finish()
        })
    }
}