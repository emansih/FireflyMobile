package xyz.hisname.fireflyiii.ui.onboarding

import android.accounts.AccountManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
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
import xyz.hisname.fireflyiii.util.extension.onAnimationEnd
import java.util.*
import android.R.mipmap
import xyz.hisname.fireflyiii.BuildConfig


class OnboardingActivity: AccountAuthenticatorActivity() {

    private val accManager by lazy { AuthenticatorManager(AccountManager.get(this))  }
    private val sharedPref by lazy {  PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_LoginTheme)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        val homeActivity = Intent(this, HomeActivity::class.java)
        if(intent.action == "xyz.hisname.fireflyiii.ADD_TRANSACTION"){
            homeActivity.putExtra("transaction", "transactionFragment")
        }
        if(installShortCut()) {
            homeActivity.putExtra("transaction", "transactionFragment")
        }
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
                    startActivity(homeActivity)
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
                startActivity(homeActivity)
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
                    supportFragmentManager.commit(allowStateLoss = true){
                        replace(R.id.fragment_container, AuthChooserFragment())
                    }
                }
            }
        }
    }

    private fun installShortCut(): Boolean{
        if (Intent.ACTION_CREATE_SHORTCUT == intent.action) {
            val shortCutIntent = Intent(this, OnboardingActivity::class.java)
            val iconResource = Intent.ShortcutIconResource.fromContext(this, R.drawable.app_icon)
            shortCutIntent.action = "xyz.hisname.fireflyiii.ADD_TRANSACTION"
            val newIntent = Intent()
            newIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortCutIntent)
                    .putExtra(Intent.EXTRA_SHORTCUT_NAME, "Add Transactions")
                    .putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)
                    .putExtra("duplicate", false)
                    .action = "com.android.launcher.action.INSTALL_SHORTCUT"
            this.sendBroadcast(newIntent)
            setResult(RESULT_OK, newIntent)
            return true
        }
        return true
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
}

