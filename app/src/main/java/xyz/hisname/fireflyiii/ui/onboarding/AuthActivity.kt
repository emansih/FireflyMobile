package xyz.hisname.fireflyiii.ui.onboarding

import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.icon
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.activity_auth.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.AccountAuthenticatorActivity
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getViewModel

class AuthActivity: AccountAuthenticatorActivity(), FragmentManager.OnBackStackChangedListener  {

    private lateinit var authActivityViewModel: AuthActivityViewModel
    private val progressOverlay by bindView<View>(R.id.progress_overlay)
    private val accountManager by lazy { AuthenticatorManager(AccountManager.get(this))  }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_LoginTheme)
        setContentView(R.layout.activity_auth)
        authActivityViewModel = getViewModel(AuthActivityViewModel::class.java)
        if(savedInstanceState == null){
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
            AppPref(sharedPref).isCustomCa = false
            supportFragmentManager.commit {
                add(R.id.container, AuthChooserFragment())
            }
        } else {
            authActivityViewModel.isShowingBack.postValue(supportFragmentManager.backStackEntryCount > 0)
        }
        supportFragmentManager.addOnBackStackChangedListener(this)
        setHelpImage()
        showLoading()
        showMessage()
        authenticated()

    }

    private fun setHelpImage(){
        helpImage.setImageDrawable(
                IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_help).apply {
                    sizeDp = 24
                }
        )
        helpImage.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, "https://github.com/emansih/FireflyMobile/wiki/Authentication".toUri())
            if (browserIntent.resolveActivity(packageManager) != null){
                startActivity(browserIntent)
            } else {
                toastError(resources.getString(R.string.no_browser_installed))
            }
        }
    }

    private fun showLoading(){
        authActivityViewModel.isLoading.observe(this){ loading ->
            if(loading){
                ProgressBar.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressOverlay, View.GONE, 0f, 200)
            }
        }
    }

    private fun showMessage(){
        authActivityViewModel.showInfoMessage.observe(this){ infoMessage ->
            toastInfo(infoMessage)
        }
        authActivityViewModel.showErrorMessage.observe(this){ errorMessage ->
            toastError(errorMessage, Toast.LENGTH_LONG)
        }
    }

    private fun authenticated(){
        authActivityViewModel.isAuthenticated.observe(this){ isAuthenticated ->
            if(isAuthenticated){
                toastSuccess(resources.getString(R.string.welcome))
                supportFragmentManager.commit {
                    setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    replace(R.id.container, OnboardingFragment())
                }
            }
        }
    }

    private fun showDialog(){
        ProgressBar.animateView(progressOverlay, View.GONE, 0f, 200)
        AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.authentication_failed))
                .setMessage(resources.getString(R.string.authentication_failed_message, Constants.REDIRECT_URI))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                }
                .create()
                .show()
    }

    override fun onResume() {
        super.onResume()
        val uri = intent.data
        if(uri != null &&  accountManager.authMethod.isBlank()){
            hideKeyboard()
            val code = uri.getQueryParameter("code")
            if(uri.toString().startsWith(Constants.REDIRECT_URI)){
                if(code != null && code.isNotBlank() && code.isNotEmpty()) {
                    authActivityViewModel.getAccessToken(code)
                } else {
                    showDialog()
                }
            }
            if(uri.toString().startsWith(Constants.DEMO_REDIRECT_URI)){
                if(code != null && code.isNotBlank() && code.isNotEmpty()) {
                    authActivityViewModel.setDemo(code)
                } else {
                    showDialog()
                }
            }
        }

    }

    override fun onBackStackChanged() {
        authActivityViewModel.isShowingBack.postValue(supportFragmentManager.backStackEntryCount > 0)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        authActivityViewModel.isLoading.postValue(false)
        FireflyClient.destroyInstance()
    }

}