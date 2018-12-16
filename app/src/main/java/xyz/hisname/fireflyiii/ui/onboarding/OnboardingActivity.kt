package xyz.hisname.fireflyiii.ui.onboarding

import android.accounts.AccountManager
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.ViewPropertyAnimator
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.ui.HomeActivity
import xyz.hisname.fireflyiii.ui.base.AccountAuthenticatorActivity
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import xyz.hisname.fireflyiii.util.DeviceUtil
import java.util.*


class OnboardingActivity: AccountAuthenticatorActivity() {

    private val accManager by lazy { AuthenticatorManager(AccountManager.get(this))  }
    private val sharedPref by lazy {  PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        if(Objects.equals("oauth", accManager.authMethod) &&
                AppPref(PreferenceManager.getDefaultSharedPreferences(this)).baseUrl.isNotEmpty()){
            if (accManager.isTokenValid()) {
                val bundle = bundleOf("ACTION" to "REFRESH_TOKEN")
                supportFragmentManager.commit{
                    replace(R.id.fragment_container, LoginFragment().apply { arguments = bundle })
                }

            } else {
                if (AppPref(sharedPref).isTransactionPersistent) {
                    NotificationUtils(this).showTransactionPersistentNotification()
                }
                GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
                    delay(500)
                    startActivity(Intent(this@OnboardingActivity, HomeActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            }
        } else if(Objects.equals("pat", accManager.authMethod) &&
                AppPref(PreferenceManager.getDefaultSharedPreferences(this)).baseUrl.isNotEmpty()){
            if(AppPref(sharedPref).isTransactionPersistent){
                NotificationUtils(this).showTransactionPersistentNotification()
            }
            GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
                delay(500)
                startActivity(Intent(this@OnboardingActivity, HomeActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        } else {
            piggyIcon.animate().apply {
                translationY(-DeviceUtil.getScreenHeight(this@OnboardingActivity).toFloat() / 2 + 160F)
                interpolator = FastOutSlowInInterpolator()
                duration = 900
                onAnimationEnd {
                    AuthenticatorManager(AccountManager.get(this@OnboardingActivity)).destroyAccount()
                    app_name_textview.isVisible = true
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, AuthChooserFragment())
                            .commit()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        RetrofitBuilder.destroyInstance()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    private inline fun ViewPropertyAnimator.onAnimationEnd(crossinline continuation: (Animator) -> Unit) {
        setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                continuation(animation)
            }
        })
    }

}

